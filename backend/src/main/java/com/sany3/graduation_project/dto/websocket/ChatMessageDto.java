package com.sany3.graduation_project.dto.websocket;

import com.sany3.graduation_project.entites.ChatMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WebSocket Chat Message DTO
 * Sent/Received over WebSocket STOMP
 *
 * Used for real-time messaging between users in a chat room
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

    /**
     * Message ID
     */
    private Long id;

    /**
     * Chat room ID this message belongs to
     */
    private Long chatRoomId;

    /**
     * Sender information
     */
    private Long senderId;
    private String senderName;
    private String senderAvatar;

    /**
     * Message content
     * - For TEXT: the actual message text
     * - For LOCATION: "latitude,longitude" format
     * - For PHOTO: URL to photo file
     */
    private String message;

    /**
     * Message type: TEXT, LOCATION, PHOTO
     */
    private ChatMessageType messageType;

    /**
     * Location data (only for LOCATION messages)
     */
    private BigDecimal latitude;
    private BigDecimal longitude;

    /**
     * When was message sent
     */
    private LocalDateTime timestamp;
}