package com.sany3.graduation_project.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Chat Message Entity
 * Individual messages between customer and provider
 *
 * Supports different message types:
 * - TEXT: Regular text message
 * - LOCATION: GPS coordinates (provider shares location)
 * - PHOTO: Image file (future feature)
 *
 * Example messages:
 *   Hassan: "Hi Ali! I'm getting ready now" (TEXT)
 *   Ali: "Great! I'm at home" (TEXT)
 *   Hassan: (shares GPS location) (LOCATION)
 *   Hassan: "I'm outside your gate" (TEXT)
 */
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_room_id", columnList = "chat_room_id"),
        @Index(name = "idx_sender_id", columnList = "sender_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Which chat room does this message belong to?
     * Many messages in one room
     *
     * Example: ChatRoom #999 has 25 messages
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * Who sent this message?
     * Either customer or provider
     *
     * Example: Hassan Khan sent this
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * The actual message content
     * Text, coordinates, or file reference depending on type
     *
     * Examples:
     *   TEXT: "I'm on my way"
     *   LOCATION: "24.7898,67.0345" (lat,lng)
     *   PHOTO: "/uploads/photo_123.jpg"
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    /**
     * Type of message:
     * - TEXT: Regular text message
     * - LOCATION: GPS coordinates
     * - PHOTO: Image file
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatMessageType messageType;

    /**
     * Latitude (for LOCATION type messages)
     * Null for TEXT messages
     *
     * Example: 24.7898 (provider's current location)
     */
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    /**
     * Longitude (for LOCATION type messages)
     * Null for TEXT messages
     *
     * Example: 67.0345 (provider's current location)
     */
    @Column(precision = 10, scale = 8)
    private BigDecimal longitude;

    /**
     * When was this message sent?
     * Auto-set by Hibernate
     *
     * Used to sort messages in chronological order
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}