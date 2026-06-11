package com.sany3.graduation_project.entites;

public enum TransactionType {
    PLATFORM_FEE("Platform Fee"),
    CREDIT_CARD_EARNING("Credit Card Earning"),
    FEE_PAYMENT("Fee Payment"),
    PAYOUT("Payout");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
