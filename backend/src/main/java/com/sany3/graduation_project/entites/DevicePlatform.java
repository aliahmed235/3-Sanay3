package com.sany3.graduation_project.entites;

import lombok.Getter;

/**
 * Mobile device platform for FCM token management
 */
@Getter
public enum DevicePlatform {
    ANDROID("Android device"),
    IOS("iOS device");

    private final String description;

    DevicePlatform(String description) {
        this.description = description;
    }
}
