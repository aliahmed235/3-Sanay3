package com.sany3.graduation_project.entites;

import lombok.Getter;

/**
 * Enumeration for service types
 * Each provider specializes in ONE type only
 */
@Getter
public enum ServiceType {
    CARPENTER("Carpenter Services"),
    WATER("Water Services"),
    ELECTRICITY("Electricity Services");

    private final String displayName;

    ServiceType(String displayName) {
        this.displayName = displayName;
    }

}
