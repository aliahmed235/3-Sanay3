package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.NotificationLogRepository;
import com.sany3.graduation_project.entites.NotificationLog;
import com.sany3.graduation_project.entites.NotificationStatus;
import com.sany3.graduation_project.entites.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Single entry point for sending a user notification:
 *  1. Persists a NotificationLog (in-app history / audit)
 *  2. Sends the push via Firebase
 *  3. Updates the log status to SENT / FAILED
 *
 * Degrades gracefully — if Firebase is not configured or the user has no
 * device token, the log is still written (status FAILED) and nothing throws.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppNotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final PushNotificationService pushNotificationService;

    /**
     * Notify a user. Never throws — notification failures must not break the
     * business action that triggered them (e.g. admin approval).
     */
    public void notify(User user, String type, String title, String body, String deepLink) {
        NotificationLog notifLog = NotificationLog.builder()
                .user(user)
                .notificationType(type)
                .title(title)
                .body(body)
                .deepLink(deepLink)
                .status(NotificationStatus.PENDING)
                .build();
        notifLog = notificationLogRepository.save(notifLog);

        try {
            Map<String, String> data = new HashMap<>();
            data.put("type", type);
            if (deepLink != null) {
                data.put("deepLink", deepLink);
            }

            String messageId = pushNotificationService.sendToUser(user.getId(), title, body, data);

            if (messageId != null) {
                notifLog.setStatus(NotificationStatus.SENT);
                notifLog.setFcmMessageId(messageId);
                notifLog.setSentAt(LocalDateTime.now());
            } else {
                notifLog.setStatus(NotificationStatus.FAILED);
                notifLog.setFailureReason("No FCM message id (Firebase off or no active device token)");
            }
        } catch (Exception e) {
            notifLog.setStatus(NotificationStatus.FAILED);
            notifLog.setFailureReason(e.getMessage());
            log.error("Notification send failed for user {}: {}", user.getId(), e.getMessage());
        }

        notificationLogRepository.save(notifLog);
    }
}
