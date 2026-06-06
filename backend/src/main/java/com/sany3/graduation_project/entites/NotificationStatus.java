package com.sany3.graduation_project.entites;

import lombok.Getter;

/**
 * Status of a push notification
 */
@Getter
public enum NotificationStatus {
    PENDING("Notification created, not yet sent"),
    SENT("Successfully sent via FCM"),
    FAILED("Failed to send"),
    OPENED("User opened the notification");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }
}
