package com.sany3.graduation_project.entites;

import lombok.Getter;

@Getter
public enum ServiceType {
    GAS("Gas Services"),
    WATER("Water Services"),
    ELECTRICITY("Electrical Services");

    private final String displayName;

    ServiceType(String displayName) {
        this.displayName = displayName;
    }

}