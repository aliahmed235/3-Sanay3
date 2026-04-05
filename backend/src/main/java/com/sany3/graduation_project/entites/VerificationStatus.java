package com.sany3.graduation_project.entites;

import lombok.Getter;

@Getter
public enum VerificationStatus {
    PENDING("Waiting for admin review"),
    APPROVED("Approved by admin"),
    REJECTED("Rejected by admin");

    private final String description;

    VerificationStatus(String description) {
        this.description = description;
    }

}