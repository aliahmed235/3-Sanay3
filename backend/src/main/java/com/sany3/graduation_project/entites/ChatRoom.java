package com.sany3.graduation_project.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chat Room Entity
 * Created automatically when customer accepts a provider's offer
 * Represents the chat between customer and provider for a specific request
 *
 * Example:
 *   Request: #456 (Gas problem)
 *   Customer: Ali Ahmed
 *   Provider: Hassan Khan
 *   → ChatRoom created linking all 3
 *   → Ali and Hassan can now message each other
 */
@Entity
@Table(name = "chat_rooms", indexes = {
        @Index(name = "idx_request_id", columnList = "request_id"),
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_provider_id", columnList = "provider_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The service request this chat is for
     * One-to-One: 1 request = 1 chat room
     *
     * Unique constraint ensures only 1 chat per request
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", unique = true, nullable = false)
    private ServiceRequest request;

    /**
     * Customer side of conversation
     *
     * Example: Ali Ahmed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    /**
     * Provider side of conversation
     *
     * Example: Hassan Khan
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    /**
     * When was this chat room created?
     * Auto-set when offer is accepted
     *
     * Usually created ~5-10 minutes after request posted
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * All messages in this chat room
     * One-to-Many: 1 room → many messages
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages;
}