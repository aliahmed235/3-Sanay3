package com.sany3.graduation_project.entites;

public enum PaymentMethod {
    CASH("Cash"),
    CREDIT_CARD("Credit Card");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
