package com.sany3.graduation_project.entites;

/**
 * Enumeration for service request status
 *
 * Lifecycle:
 * OPEN → ACCEPTED → ONGOING → COMPLETED
 *                           ↘ CANCELLED
 */
public enum RequestStatus {

    /**
     * Request is open and waiting for provider offers
     * Customers see: "Waiting for offers..."
     * Providers see: This request on their map
     */
    OPEN("Open - Waiting for offers"),

    /**
     * Customer has accepted a provider's offer
     * Chat room is now active
     * Provider is on the way
     */
    ACCEPTED("Accepted - Provider on the way"),

    /**
     * Service is in progress
     * Customer scanned QR code #1
     * Timer is running
     */
    ONGOING("Service in progress"),

    /**
     * Service is complete
     * Customer scanned QR code #2
     * Waiting for rating
     */
    COMPLETED("Completed"),

    /**
     * Request was cancelled
     * Either by customer or system (expired)
     */
    CANCELLED("Cancelled");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}