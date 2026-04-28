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
 * Individual message in a chat room
 *
 * Types of messages:
 * 1. TEXT: "I'll be there in 10 minutes"
 * 2. LOCATION: "Here's my current location" (lat/long)
 * 3. IMAGE: "Photo of the problem"
 *
 * Example:
 *   Provider sends: "I'm 5 minutes away"
 *   → ChatMessage created, stored in DB
 *   → Sent to customer via WebSocket (real-time)
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
     *
     * Example: ChatRoom #789 (for Request #456)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * Who sent this message?
     * Can be customer or provider
     *
     * Example: Hassan Khan (provider)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * The message text/content
     * Can be:
     * - "I'm on my way"
     * - "URL to image"
     * - "Image description"
     *
     * Max 1000 characters
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * Type of message
     * TEXT, LOCATION, IMAGE
     *
     * Example: TEXT
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private ChatMessageType messageType;

    /**
     * If message type is LOCATION, store latitude
     *
     * Example: 24.8607 (Karachi)
     */
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    /**
     * If message type is LOCATION, store longitude
     *
     * Example: 67.0011 (Karachi)
     */
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    /**
     * When was this message sent?
     * Auto-set, never changes
     *
     * Example: 2026-04-25T14:30:00
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}