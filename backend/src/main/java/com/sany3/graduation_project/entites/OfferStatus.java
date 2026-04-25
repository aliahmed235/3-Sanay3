package com.sany3.graduation_project.entites;

/**
 * Enumeration for service offer status
 *
 * Lifecycle:
 * PENDING → ACCEPTED  (Customer chose this provider)
 *        → REJECTED   (Customer chose someone else)
 *        → WITHDRAWN  (Provider took back the offer)
 */
public enum OfferStatus {

    /**
     * Waiting for customer response
     * Customer hasn't seen it yet or is considering
     *
     * Provider can still withdraw it
     * Customer can still reject or accept
     */
    PENDING("Pending - Waiting for customer response"),

    /**
     * Customer accepted this offer! 🎉
     * Chat room is created
     * Service will happen soon
     *
     * All other offers for same request are rejected automatically
     */
    ACCEPTED("Accepted - Provider confirmed"),

    /**
     * Customer rejected this offer
     * They chose a different provider
     *
     * Provider can still make new offer for future requests
     */
    REJECTED("Rejected - Customer chose another provider"),

    /**
     * Provider withdrew their offer
     * They changed their mind or got busy
     *
     * Customer can still see other offers
     */
    WITHDRAWN("Withdrawn - Provider cancelled offer");

    private final String displayName;

    OfferStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}