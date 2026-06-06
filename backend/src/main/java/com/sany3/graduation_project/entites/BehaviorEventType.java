package com.sany3.graduation_project.entites;

import lombok.Getter;

/**
 * Types of user behavior events tracked for AI nudge logic
 */
@Getter
public enum BehaviorEventType {
    APP_OPENED("User opened the app"),
    SERVICE_TYPE_VIEWED("User viewed a service type category"),
    SERVICE_REQUEST_STARTED("User started filling out a service request form"),
    SERVICE_REQUEST_CREATED("User successfully created a service request"),
    NOTIFICATION_OPENED("User tapped on a push notification");

    private final String description;

    BehaviorEventType(String description) {
        this.description = description;
    }
}
