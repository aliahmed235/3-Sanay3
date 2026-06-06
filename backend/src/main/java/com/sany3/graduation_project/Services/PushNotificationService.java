package com.sany3.graduation_project.Services;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.sany3.graduation_project.Repositories.UserFcmTokenRepository;
import com.sany3.graduation_project.entites.UserFcmToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Sends push notifications via Firebase Cloud Messaging.
 * Gracefully degrades when Firebase is not configured.
 */
@Service
@Slf4j
public class PushNotificationService {

    @Autowired(required = false)
    private FirebaseApp firebaseApp;

    private final UserFcmTokenRepository fcmTokenRepository;

    public PushNotificationService(UserFcmTokenRepository fcmTokenRepository) {
        this.fcmTokenRepository = fcmTokenRepository;
    }

    /**
     * Send a push notification to all active devices of a user.
     *
     * @param userId User ID
     * @param title  Notification title
     * @param body   Notification body
     * @param data   Extra data payload (optional)
     * @return FCM message ID if sent, null if Firebase not configured or no tokens
     */
    public String sendToUser(Long userId, String title, String body, Map<String, String> data) {
        if (firebaseApp == null) {
            log.warn("Firebase not configured — skipping push notification for user {}", userId);
            return null;
        }

        List<UserFcmToken> tokens = fcmTokenRepository.findByUserIdAndActiveTrue(userId);

        if (tokens.isEmpty()) {
            log.warn("No active FCM tokens for user {}", userId);
            return null;
        }

        String lastMessageId = null;

        for (UserFcmToken fcmToken : tokens) {
            try {
                Message.Builder messageBuilder = Message.builder()
                        .setToken(fcmToken.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build());

                if (data != null && !data.isEmpty()) {
                    messageBuilder.putAllData(data);
                }

                String messageId = FirebaseMessaging.getInstance(firebaseApp)
                        .send(messageBuilder.build());

                log.info("FCM sent to user {} token {}: messageId={}", userId, fcmToken.getId(), messageId);
                lastMessageId = messageId;

            } catch (FirebaseMessagingException e) {
                log.error("FCM send failed for user {} token {}: {}", userId, fcmToken.getId(), e.getMessage());

                // Deactivate invalid tokens
                if ("UNREGISTERED".equals(e.getMessagingErrorCode().name())
                        || "INVALID_ARGUMENT".equals(e.getMessagingErrorCode().name())) {
                    log.info("Deactivating invalid FCM token {} for user {}", fcmToken.getId(), userId);
                    fcmToken.setActive(false);
                    fcmTokenRepository.save(fcmToken);
                }
            }
        }

        return lastMessageId;
    }

    /**
     * Check if Firebase is configured and available
     */
    public boolean isFirebaseConfigured() {
        return firebaseApp != null;
    }
}
