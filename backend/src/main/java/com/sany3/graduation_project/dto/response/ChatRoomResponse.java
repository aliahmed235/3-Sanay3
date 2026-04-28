package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Response for chat room
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponse {

    /**
     * Chat room ID
     */
    private Long id;

    /**
     * Request ID this chat is for
     */
    private Long requestId;
    private String requestTitle;

    /**
     * Customer info
     */
    private Long customerId;
    private String customerName;
    private String customerAvatar;

    /**
     * Provider info
     */
    private Long providerId;
    private String providerName;
    private String providerAvatar;

    /**
     * Chat stats
     */
    private Long messageCount;
    private String lastMessage;
    private LocalDateTime lastMessageTime;

    /**
     * When created
     */
    private LocalDateTime createdAt;
}