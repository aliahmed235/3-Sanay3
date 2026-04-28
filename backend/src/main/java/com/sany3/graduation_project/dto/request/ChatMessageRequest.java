package com.sany3.graduation_project.dto.request;

import com.sany3.graduation_project.entites.ChatMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Chat message sent from client
 * Can be sent via REST endpoint or WebSocket
 *
 * Example TEXT message:
 * {
 *   "chatRoomId": 1,
 *   "message": "I'm on my way",
 *   "messageType": "TEXT"
 * }
 *
 * Example LOCATION message:
 * {
 *   "chatRoomId": 1,
 *   "message": "",
 *   "messageType": "LOCATION",
 *   "latitude": 24.8607,
 *   "longitude": 67.0011
 * }
 *
 * Example PHOTO message:
 * {
 *   "chatRoomId": 1,
 *   "message": "/uploads/photo_123.jpg",
 *   "messageType": "PHOTO"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {

    /**
     * Chat room ID
     */
    @NotNull(message = "Chat room ID is required")
    private Long chatRoomId;

    /**
     * Message content
     * Required for TEXT and PHOTO
     * Empty for LOCATION (coordinates in latitude/longitude)
     */
    private String message;

    /**
     * Message type
     */
    @NotNull(message = "Message type is required")
    private ChatMessageType messageType;

    /**
     * For LOCATION messages only
     */
    private BigDecimal latitude;
    private BigDecimal longitude;
}