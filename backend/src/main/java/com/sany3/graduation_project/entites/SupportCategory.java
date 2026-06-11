package com.sany3.graduation_project.entites;

public enum SupportCategory {

    // Customer categories
    LATE_PROVIDER("Provider arrived late", "USER"),
    BAD_SERVICE("Poor service quality", "USER"),

    // Provider categories
    CUSTOMER_NO_SHOW("Customer wasn't available", "SERVICE_PROVIDER"),
    UNFAIR_RATING("Dispute a rating", "SERVICE_PROVIDER"),
    PAYMENT_DELAYED("Haven't received payout", "SERVICE_PROVIDER"),
    ACCOUNT_ISSUE("Account or ban issue", "SERVICE_PROVIDER"),

    // Shared categories (both roles)
    PAYMENT_ISSUE("Payment problem", "BOTH"),
    APP_BUG("App issue or bug", "BOTH"),
    SAFETY_CONCERN("Safety concern", "BOTH"),
    OTHER("Other", "BOTH");

    private final String displayName;
    private final String allowedRole;

    SupportCategory(String displayName, String allowedRole) {
        this.displayName = displayName;
        this.allowedRole = allowedRole;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAllowedRole() {
        return allowedRole;
    }

    public boolean isAllowedFor(String role) {
        return "BOTH".equals(allowedRole) || allowedRole.equals(role);
    }
}
