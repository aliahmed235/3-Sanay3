package com.sany3.graduation_project.entites;

/**
 * Enumeration for service offer status
 *
 * Lifecycle:
 * PENDING → ACCEPTED  (Customer chose this provider)
 *        → REJECTED   (Customer chose someone else)
 *        → WITHDRAWN  (Provider took back the offer)
 * ACCEPTED → ONGOING  (Provider started the service)
 * ONGOING → COMPLETED (Provider finished the service)
 */
public enum OfferStatus {

    PENDING("Pending - Waiting for customer response"),

    ACCEPTED("Accepted - Provider confirmed"),

    ONGOING("Ongoing - Service in progress"),

    COMPLETED("Completed - Service finished"),

    REJECTED("Rejected - Customer chose another provider"),

    WITHDRAWN("Withdrawn - Provider cancelled offer");

    private final String displayName;

    OfferStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}