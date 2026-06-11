package com.sany3.graduation_project.entites;

public enum SupportCategory {

    LATE_PROVIDER("Provider arrived late"),
    BAD_SERVICE("Poor service quality"),
    PAYMENT_ISSUE("Payment problem"),
    APP_BUG("App issue or bug"),
    SAFETY_CONCERN("Safety concern"),
    OTHER("Other");

    private final String displayName;

    SupportCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
