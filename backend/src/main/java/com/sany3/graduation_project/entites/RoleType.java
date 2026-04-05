package com.sany3.graduation_project.entites;

import lombok.Getter;

@Getter
public enum RoleType {
    USER("Customer can make service requests"),
    SERVICE_PROVIDER("Service provider can accept requests and earn"),
    ADMIN("Admin can verify providers and manage platform");

    private final String description;

    RoleType(String description) {
        this.description = description;
    }

}