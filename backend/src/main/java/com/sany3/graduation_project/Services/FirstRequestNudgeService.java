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

    private final AtomicBoolean running = new AtomicBoolean(false);

    private static final String NOTIFICATION_TYPE = Constants.NUDGE.NOTIFICATION_TYPE;

    /**
     * Scheduled job — runs every hour
     */
    @Scheduled(fixedDelay = 3600000) // 1 hour
    public void scheduledNudgeJob() {
        log.info("Scheduled nudge job triggered");
        runNudgeJob();
    }

    /**
     * Manual trigger for admin endpoint
     */
    public NudgeJobResultResponse triggerNudgeJobManually() {
        log.info("Manual nudge job triggered by admin");
        return runNudgeJob();
    }

    /**
     * Core nudge logic
     */
    @Transactional
    public NudgeJobResultResponse runNudgeJob() {
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
                if (!isEligibleForNudge(userId)) {
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
     * Anti-spam rules:
     * 1. No prior nudge of this type
     * 2. User has been idle for at least 1 hour
     * 3. User activity is within 24 hours
     * 4. User has at least one active FCM token
     */
    private boolean isEligibleForNudge(Long userId) {
        // 1. Already nudged?
        if (notificationLogRepository.existsByUserIdAndNotificationType(userId, NOTIFICATION_TYPE)) {
            return false;
        }

        // 2. Last activity check
        LocalDateTime lastActivity = eventRepository.findLatestEventTimeByUserId(userId);
        if (lastActivity == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesSinceActivity = java.time.Duration.between(lastActivity, now).toMinutes();

        // Must be idle for at least 1 hour
        if (minutesSinceActivity < Constants.NUDGE.MIN_IDLE_MINUTES) {
            return false;
        }

        // Must be within 24 hours
        if (minutesSinceActivity > Constants.NUDGE.MAX_AGE_HOURS * 60) {
            return false;
        }

        // 3. Has FCM token?
        List<UserFcmToken> tokens = fcmTokenRepository.findByUserIdAndActiveTrue(userId);
        if (tokens.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Evaluate which nudge rule applies.
     * Priority:
     * 1. NIGHT_URGENCY — opened app at night (9PM-5AM)
     * 2. SERVICE_INTEREST — viewed service type or started request form
     * 3. GENERIC — just opened the app
     */
    private NudgeResult evaluateRules(Long userId, List<UserBehaviorEvent> events) {
        if (events.isEmpty()) {
            return null;
        }

        // Check for night urgency
        UserBehaviorEvent latestAppOpen = events.stream()
                .filter(e -> e.getEventType() == BehaviorEventType.APP_OPENED)
                .findFirst()
                .orElse(null);

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

        // Check for service interest
        UserBehaviorEvent serviceEvent = events.stream()
                .filter(e -> e.getEventType() == BehaviorEventType.SERVICE_TYPE_VIEWED
                        || e.getEventType() == BehaviorEventType.SERVICE_REQUEST_STARTED)
                .findFirst()
                .orElse(null);

        if (serviceEvent != null && serviceEvent.getServiceType() != null) {
            String serviceDisplayName = serviceEvent.getServiceType().getDisplayName();
            return new NudgeResult(
                    Constants.NUDGE.RULE_SERVICE_INTEREST,
                    "Still need help with " + serviceDisplayName + "?",
                    "Verified providers are ready to help. Post your request in under a minute.",
                    "app://new-request?serviceType=" + serviceEvent.getServiceType().name(),
                    serviceEvent.getServiceType(),
                    "User viewed " + serviceDisplayName + " — showing interest in this service"
            );
        }

        // Generic fallback
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
