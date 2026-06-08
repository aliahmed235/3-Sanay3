package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.NotificationLogRepository;
import com.sany3.graduation_project.Repositories.UserBehaviorEventRepository;
import com.sany3.graduation_project.Repositories.UserFcmTokenRepository;
import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.dto.response.NudgeJobResultResponse;
import com.sany3.graduation_project.entites.*;
import com.sany3.graduation_project.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI-driven nudge service that sends smart push notifications
 * to users who showed interest but didn't create their first ServiceRequest.
 *
 * Runs every hour via @Scheduled. Can also be triggered manually by admin.
 *
 * Rules (priority order):
 * 1. NIGHT_URGENCY — User opened app between 9PM-5AM
 * 2. SERVICE_INTEREST — User viewed a service type or started a request form
 * 3. GENERIC — User only opened the app
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FirstRequestNudgeService {

    private final UserBehaviorEventRepository eventRepository;
    private final UserFcmTokenRepository fcmTokenRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;
    private final MlScoringService mlScoringService;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private static final String NOTIFICATION_TYPE = Constants.NUDGE.NOTIFICATION_TYPE;

    /**
     * Scheduled job — runs every hour (full checks)
     */
    @Scheduled(fixedDelay = 3600000) // 1 hour
    public void scheduledNudgeJob() {
        log.info("Scheduled nudge job triggered");
        runNudgeJob(false);
    }

    /**
     * Manual trigger for admin endpoint (skips idle + anti-spam for testing)
     */
    public NudgeJobResultResponse triggerNudgeJobManually() {
        log.info("Manual nudge job triggered by admin (skipping idle check)");
        return runNudgeJob(true);
    }

    /**
     * Core nudge logic
     * @param skipChecks if true, skips anti-spam + idle check (for manual admin testing)
     */
    @Transactional
    public NudgeJobResultResponse runNudgeJob(boolean skipChecks) {
        if (!running.compareAndSet(false, true)) {
            log.warn("Nudge job already running — skipping");
            return NudgeJobResultResponse.builder()
                    .eligibleUsers(0)
                    .notificationsSent(0)
                    .notificationsFailed(0)
                    .triggeredAt(LocalDateTime.now())
                    .build();
        }

        try {
            LocalDateTime since = LocalDateTime.now().minusHours(Constants.NUDGE.MAX_AGE_HOURS);

            // Find users who showed intent but never created a request
            List<Long> candidateUserIds = eventRepository.findUsersWithIntentButNoRequest(since);
            log.info("Found {} candidate users for nudge", candidateUserIds.size());

            int eligible = 0;
            int sent = 0;
            int failed = 0;

            for (Long userId : candidateUserIds) {
                if (!isEligibleForNudge(userId, skipChecks)) {
                    continue;
                }
                eligible++;

                // Get user's events to determine the best rule
                List<UserBehaviorEvent> events = eventRepository.findLatestByUserAndTypes(userId,
                        List.of(BehaviorEventType.APP_OPENED,
                                BehaviorEventType.SERVICE_TYPE_VIEWED,
                                BehaviorEventType.SERVICE_REQUEST_STARTED));

                NudgeResult nudge = evaluateRules(userId, events);

                if (nudge == null) {
                    continue;
                }

                // Save notification log
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) continue;

                NotificationLog notifLog = NotificationLog.builder()
                        .user(user)
                        .notificationType(NOTIFICATION_TYPE)
                        .ruleName(nudge.ruleName)
                        .serviceType(nudge.serviceType)
                        .title(nudge.title)
                        .body(nudge.body)
                        .deepLink(nudge.deepLink)
                        .aiExplanation(nudge.explanation)
                        .status(NotificationStatus.PENDING)
                        .build();

                notifLog = notificationLogRepository.save(notifLog);

                // Send FCM
                Map<String, String> data = new HashMap<>();
                data.put("type", NOTIFICATION_TYPE);
                data.put("notificationId", notifLog.getId().toString());
                if (nudge.deepLink != null) {
                    data.put("deepLink", nudge.deepLink);
                }

                String messageId = pushNotificationService.sendToUser(userId, nudge.title, nudge.body, data);

                if (messageId != null) {
                    notifLog.setStatus(NotificationStatus.SENT);
                    notifLog.setFcmMessageId(messageId);
                    notifLog.setSentAt(LocalDateTime.now());
                    sent++;
                } else {
                    notifLog.setStatus(NotificationStatus.FAILED);
                    notifLog.setFailureReason(
                            pushNotificationService.isFirebaseConfigured()
                                    ? "No active FCM tokens"
                                    : "Firebase not configured");
                    failed++;
                }

                notificationLogRepository.save(notifLog);
            }

            log.info("Nudge job complete: eligible={}, sent={}, failed={}", eligible, sent, failed);

            return NudgeJobResultResponse.builder()
                    .eligibleUsers(eligible)
                    .notificationsSent(sent)
                    .notificationsFailed(failed)
                    .triggeredAt(LocalDateTime.now())
                    .build();

        } finally {
            running.set(false);
        }
    }

    /**
     * Check if user is eligible for a nudge notification.
     * When skipChecks=true (manual admin trigger), skips anti-spam + idle check for unlimited testing.
     */
    private boolean isEligibleForNudge(Long userId, boolean skipChecks) {
        // 1. Already nudged? (skip for manual trigger)
        if (!skipChecks && notificationLogRepository.existsByUserIdAndNotificationType(userId, NOTIFICATION_TYPE)) {
            log.debug("User {} already nudged — skipping", userId);
            return false;
        }

        // 2. ML model check (always runs — even manual)
        if (!mlScoringService.shouldNudge(userId)) {
            log.info("ML model decided not to nudge user {}", userId);
            return false;
        }

        // 3. Last activity check
        LocalDateTime lastActivity = eventRepository.findLatestEventTimeByUserId(userId);
        if (lastActivity == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesSinceActivity = java.time.Duration.between(lastActivity, now).toMinutes();

        // Must be idle for at least 1 hour (skip for manual trigger)
        if (!skipChecks && minutesSinceActivity < Constants.NUDGE.MIN_IDLE_MINUTES) {
            log.debug("User {} active {} min ago — not idle enough", userId, minutesSinceActivity);
            return false;
        }

        // Must be within 24 hours (skip for manual trigger)
        if (!skipChecks && minutesSinceActivity > Constants.NUDGE.MAX_AGE_HOURS * 60) {
            log.debug("User {} last active {} min ago — too old", userId, minutesSinceActivity);
            return false;
        }

        // 4. Has FCM token?
        List<UserFcmToken> tokens = fcmTokenRepository.findByUserIdAndActiveTrue(userId);
        if (tokens.isEmpty()) {
            log.debug("User {} has no active FCM tokens", userId);
            return false;
        }

        return true;
    }

    /**
     * Evaluate which nudge rule applies.
     * Priority (first match wins):
     * 1. NIGHT_URGENCY    — User opened app between 9PM-5AM (urgent need)
     * 2. ABANDONED_FORM   — User started filling a request form but didn't submit
     * 3. SERVICE_INTEREST  — User browsed a service category
     * 4. REPEAT_VISITOR    — User opened the app 3+ times but never did anything
     * 5. GENERIC           — User opened the app once (fallback)
     */
    private NudgeResult evaluateRules(Long userId, List<UserBehaviorEvent> events) {
        if (events.isEmpty()) {
            return null;
        }

        // Collect stats from events
        UserBehaviorEvent latestAppOpen = null;
        UserBehaviorEvent latestFormStart = null;
        UserBehaviorEvent latestServiceView = null;
        int appOpenCount = 0;

        for (UserBehaviorEvent e : events) {
            switch (e.getEventType()) {
                case APP_OPENED -> {
                    appOpenCount++;
                    if (latestAppOpen == null) latestAppOpen = e; // already sorted DESC
                }
                case SERVICE_REQUEST_STARTED -> {
                    if (latestFormStart == null) latestFormStart = e;
                }
                case SERVICE_TYPE_VIEWED -> {
                    if (latestServiceView == null) latestServiceView = e;
                }
                default -> {}
            }
        }

        // ── Rule 1: NIGHT_URGENCY ──
        // User opened app late at night → probably has an urgent problem
        if (latestAppOpen != null) {
            LocalTime eventTime = latestAppOpen.getOccurredAt().toLocalTime();
            if (eventTime.isAfter(LocalTime.of(21, 0)) || eventTime.isBefore(LocalTime.of(5, 0))) {
                return new NudgeResult(
                        Constants.NUDGE.RULE_NIGHT_URGENCY,
                        "Need urgent help tonight?",
                        "Post a request now and get a response within minutes — even at this hour.",
                        "app://new-request",
                        null,
                        "User opened app between 9PM-5AM, likely has an urgent need"
                );
            }
        }

        // ── Rule 2: ABANDONED_FORM ──
        // User started filling a request form but didn't finish → almost converted
        if (latestFormStart != null) {
            ServiceType serviceType = latestFormStart.getServiceType();
            String serviceName = serviceType != null ? serviceType.getDisplayName() : "your service";
            String deepLink = serviceType != null
                    ? "app://new-request?serviceType=" + serviceType.name()
                    : "app://new-request";
            return new NudgeResult(
                    Constants.NUDGE.RULE_ABANDONED_FORM,
                    "You were almost done!",
                    "Finish your " + serviceName + " request — it only takes 30 seconds.",
                    deepLink,
                    serviceType,
                    "User started form for " + serviceName + " but didn't submit — high intent"
            );
        }

        // ── Rule 3: SERVICE_INTEREST ──
        // User browsed a service category → knows what they need but hesitating
        if (latestServiceView != null && latestServiceView.getServiceType() != null) {
            String serviceDisplayName = latestServiceView.getServiceType().getDisplayName();
            return new NudgeResult(
                    Constants.NUDGE.RULE_SERVICE_INTEREST,
                    "Still need help with " + serviceDisplayName + "?",
                    "Verified providers are ready to help. Post your request in under a minute.",
                    "app://new-request?serviceType=" + latestServiceView.getServiceType().name(),
                    latestServiceView.getServiceType(),
                    "User viewed " + serviceDisplayName + " — showing interest but didn't act"
                );
        }

        // ── Rule 4: REPEAT_VISITOR ──
        // User opened app 3+ times but never browsed or started → curious but lost
        if (appOpenCount >= 3) {
            return new NudgeResult(
                    Constants.NUDGE.RULE_REPEAT_VISITOR,
                    "Looking for something?",
                    "Browse our services — plumbing, electrical, carpentry and more. Help is one tap away.",
                    "app://services",
                    null,
                    "User opened app " + appOpenCount + " times but never browsed services — needs guidance"
            );
        }

        // ── Rule 5: GENERIC (fallback) ──
        // User opened app once and left → low intent, gentle nudge
        return new NudgeResult(
                Constants.NUDGE.RULE_GENERIC,
                "Need home service help?",
                "Tell us what you need and get offers from verified local providers.",
                "app://new-request",
                null,
                "User opened the app but didn't take further action"
        );
    }

    /**
     * Internal result holder for nudge evaluation
     */
    private record NudgeResult(
            String ruleName,
            String title,
            String body,
            String deepLink,
            ServiceType serviceType,
            String explanation
    ) {}
}
