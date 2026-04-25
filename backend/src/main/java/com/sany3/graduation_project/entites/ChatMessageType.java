package com.sany3.graduation_project.entites;

/**
 * Enumeration for chat message types
 * Different kinds of messages that can be sent
 */
public enum ChatMessageType {

    /**
     * Regular text message
     * Example: "I'm on my way"
     *
     * Fields used:
     * - message: The text
     * - latitude: null
     * - longitude: null
     */
    TEXT("Text message"),

    /**
     * Location sharing message
     * Provider shares their GPS coordinates
     *
     * Fields used:
     * - message: "24.7898,67.0345"
     * - latitude: 24.7898
     * - longitude: 67.0345
     *
     * Customer sees: "Hassan shared location"
     *               Map showing Hassan 2.3 km away
     *               ETA: 12 minutes
     */
    LOCATION("Location shared"),

    /**
     * Photo/Image message
     * Provider can send photos of work
     *
     * Fields used:
     * - message: "/uploads/photo_123.jpg"
     * - latitude: null
     * - longitude: null
     *
     * Future feature - not implemented yet
     */
    PHOTO("Photo message");

    private final String displayName;

    ChatMessageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}