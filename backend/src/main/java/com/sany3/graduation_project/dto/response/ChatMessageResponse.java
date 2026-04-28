package com.sany3.graduation_project.dto.response;

import com.sany3.graduation_project.entites.ChatMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Chat message response for REST endpoints
 * Sent when fetching messages via HTTP
 *
 * Example:
 * {
 *   "id": 1,
 *   "chatRoomId": 1,
 *   "senderId": 123,
 *   "senderName": "Hassan",
 *   "senderAvatar": "https://...",
 *   "message": "I'm on my way",
 *   "messageType": "TEXT",
 *   "latitude": null,
 *   "longitude": null,
 *   "timestamp": "2026-04-28T10:30:00",
 *   "isOwn": true
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

    /**
     * Message ID
     */
    private Long id;

    /**
     * Chat room ID
     */
    private Long chatRoomId;

    /**
     * Sender info
     */
    private Long senderId;
    private String senderName;
    private String senderAvatar;

    /**
     * Message content
     */
    private String message;

    /**
     * Message type
     */
    private ChatMessageType messageType;

    /**
     * Location coordinates (for LOCATION type)
     */
    private BigDecimal latitude;
    private BigDecimal longitude;

    /**
     * Timestamp
     */
    private LocalDateTime timestamp;

    /**
     * For UI - show if sender is current user
     */
    private Boolean isOwn;
}