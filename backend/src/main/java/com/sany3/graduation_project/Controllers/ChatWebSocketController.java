package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.ChatService;
import com.sany3.graduation_project.dto.request.ChatMessageRequest;
import com.sany3.graduation_project.dto.websocket.ChatMessageDto;
import com.sany3.graduation_project.entites.ChatMessage;
import com.sany3.graduation_project.entites.ChatMessageType;
import com.sany3.graduation_project.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.time.LocalDateTime;

/**
 * WebSocket Chat Controller
 * Handles real-time messaging via STOMP protocol
 *
 * Client Flow:
 * 1. Connect to /ws
 * 2. Subscribe to /topic/chatRoom/{roomId}
 * 3. Send message to /app/chat/send/{roomId}
 * 4. Receive message from /topic/chatRoom/{roomId}
 *
 * Note: Uses @Controller not @RestController because this handles WebSocket messages
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageMapper chatMessageMapper;

    /**
     * Handle incoming chat message
     * Client sends to: /app/chat/send/{roomId}
     *
     * Example request body:
     * {
     *   "chatRoomId": 1,
     *   "message": "I'm on my way",
     *   "messageType": "TEXT"
     * }
     *
     * Response broadcasts to: /topic/chatRoom/{roomId}
     */
    @MessageMapping("/chat/send/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageRequest request,
            Principal principal) {
        String userName = authenticatedUserName(principal);
        if (userName == null) {
            log.warn("Rejected unauthenticated WebSocket message for room {}", roomId);
            return;
        }

        log.info("Received message in room: {} from user: {}", roomId, userName);

        try {
            Long senderId = Long.parseLong(userName);

            // Validate chat room ID matches
            if (!request.getChatRoomId().equals(roomId)) {
                sendErrorToUser(userName, "Chat room ID mismatch");
                return;
            }

            // Save message to database based on type
            ChatMessage savedMessage = processMessageByType(request, senderId);

            if (savedMessage == null) {
                sendErrorToUser(userName, "Failed to save message");
                return;
            }

            // Convert to DTO
            ChatMessageDto messageDto = chatMessageMapper.toChatMessageDto(savedMessage);

            // Broadcast to all users in room
            messagingTemplate.convertAndSend(
                    "/topic/chatRoom/" + roomId,
                    messageDto
            );

            log.info("Message broadcast to room: {} | Message ID: {}", roomId, savedMessage.getId());

        } catch (Exception e) {
            log.error("Error sending message: ", e);
            sendErrorToUser(userName, "Error sending message: " + e.getMessage());
        }
    }

    /**
     * Process message based on type
     */
    private ChatMessage processMessageByType(ChatMessageRequest request, Long senderId) {
        switch (request.getMessageType()) {
            case TEXT:
                return chatService.sendTextMessage(
                        request.getChatRoomId(),
                        senderId,
                        request.getMessage()
                );

            case LOCATION:
                return chatService.sendLocationMessage(
                        request.getChatRoomId(),
                        senderId,
                        request.getLatitude(),
                        request.getLongitude()
                );

            case PHOTO:
                return chatService.sendPhotoMessage(
                        request.getChatRoomId(),
                        senderId,
                        request.getMessage()
                );

            default:
                log.warn("Unknown message type: {}", request.getMessageType());
                return null;
        }
    }

    /**
     * Handle typing indicator
     * Client sends to: /app/chat/typing/{roomId}
     *
     * Notifies other users that someone is typing
     * Do NOT broadcast back to sender
     */
    @MessageMapping("/chat/typing/{roomId}")
    public void typingIndicator(
            @DestinationVariable Long roomId,
            Principal principal) {
        String userName = authenticatedUserName(principal);
        if (userName == null) {
            return;
        }

        log.debug("User {} is typing in room {}", userName, roomId);

        try {
            Long userId = Long.parseLong(userName);

            TypingEvent typingEvent = TypingEvent.builder()
                    .userId(userId)
                    .isTyping(true)
                    .build();

            // Broadcast to room (excluding sender)
            messagingTemplate.convertAndSend(
                    "/topic/chatRoom/" + roomId + "/typing",
                    typingEvent
            );
        } catch (Exception e) {
            log.error("Error sending typing indicator: ", e);
        }
    }

    /**
     * Stop typing indicator
     * Client sends to: /app/chat/stop-typing/{roomId}
     */
    @MessageMapping("/chat/stop-typing/{roomId}")
    public void stopTyping(
            @DestinationVariable Long roomId,
            Principal principal) {
        String userName = authenticatedUserName(principal);
        if (userName == null) {
            return;
        }

        log.debug("User {} stopped typing in room {}", userName, roomId);

        try {
            Long userId = Long.parseLong(userName);

            TypingEvent typingEvent = TypingEvent.builder()
                    .userId(userId)
                    .isTyping(false)
                    .build();

            messagingTemplate.convertAndSend(
                    "/topic/chatRoom/" + roomId + "/typing",
                    typingEvent
            );
        } catch (Exception e) {
            log.error("Error sending stop typing indicator: ", e);
        }
    }

    /**
     * Handle user online
     * Client sends to: /app/chat/user-online/{roomId}
     */
    @MessageMapping("/chat/user-online/{roomId}")
    public void userOnline(
            @DestinationVariable Long roomId,
            Principal principal) {
        String userName = authenticatedUserName(principal);
        if (userName == null) {
            return;
        }

        log.debug("User {} came online in room {}", userName, roomId);

        try {
            Long userId = Long.parseLong(userName);

            OnlineEvent onlineEvent = OnlineEvent.builder()
                    .userId(userId)
                    .isOnline(true)
                    .build();

            messagingTemplate.convertAndSend(
                    "/topic/chatRoom/" + roomId + "/online",
                    onlineEvent
            );
        } catch (Exception e) {
            log.error("Error sending online event: ", e);
        }
    }

    /**
     * Handle user offline
     * Client sends to: /app/chat/user-offline/{roomId}
     */
    @MessageMapping("/chat/user-offline/{roomId}")
    public void userOffline(
            @DestinationVariable Long roomId,
            Principal principal) {
        String userName = authenticatedUserName(principal);
        if (userName == null) {
            return;
        }

        log.debug("User {} went offline in room {}", userName, roomId);

        try {
            Long userId = Long.parseLong(userName);

            OnlineEvent onlineEvent = OnlineEvent.builder()
                    .userId(userId)
                    .isOnline(false)
                    .build();

            messagingTemplate.convertAndSend(
                    "/topic/chatRoom/" + roomId + "/online",
                    onlineEvent
            );
        } catch (Exception e) {
            log.error("Error sending offline event: ", e);
        }
    }

    /**
     * Send error to specific user (private message)
     */
    private void sendErrorToUser(String userName, String errorMessage) {
        messagingTemplate.convertAndSendToUser(
                userName,
                "/queue/error",
                ErrorEvent.builder()
                        .error(errorMessage)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    private String authenticatedUserName(Principal principal) {
        return principal == null ? null : principal.getName();
    }

    /**
     * Typing indicator event
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class TypingEvent {
        private Long userId;
        private Boolean isTyping;
    }

    /**
     * Online status event
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class OnlineEvent {
        private Long userId;
        private Boolean isOnline;
    }

    /**
     * Error event
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class ErrorEvent {
        private String error;
        private LocalDateTime timestamp;
    }
}
