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
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageMapper chatMessageMapper;

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

            String validationError = validateMessageRequest(roomId, request);
            if (validationError != null) {
                sendErrorToUser(userName, validationError);
                return;
            }
            ChatMessage savedMessage = processMessageByType(request, senderId);

            if (savedMessage == null) {
                sendErrorToUser(userName, "Failed to save message");
                return;
            }
            ChatMessageDto messageDto = chatMessageMapper.toChatMessageDto(savedMessage);

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
    private String validateMessageRequest(Long roomId, ChatMessageRequest request) {
        if (request == null) {
            return "Message payload is required";
        }

        if (!Objects.equals(request.getChatRoomId(), roomId)) {
            return "Chat room ID mismatch";
        }

        if (request.getMessageType() == null) {
            return "Message type is required";
        }

        if ((request.getMessageType() == ChatMessageType.TEXT || request.getMessageType() == ChatMessageType.PHOTO)
                && (request.getMessage() == null || request.getMessage().isBlank())) {
            return "Message content is required";
        }

        if (request.getMessageType() == ChatMessageType.LOCATION
                && (request.getLatitude() == null || request.getLongitude() == null)) {
            return "Latitude and longitude are required";
        }

        return null;
    }
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

            messagingTemplate.convertAndSend(
                    "/topic/chatRoom/" + roomId + "/typing",
                    typingEvent
            );
        } catch (Exception e) {
            log.error("Error sending typing indicator: ", e);
        }
    }
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

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class TypingEvent {
        private Long userId;
        private Boolean isTyping;
    }
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class OnlineEvent {
        private Long userId;
        private Boolean isOnline;
    }
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class ErrorEvent {
        private String error;
        private LocalDateTime timestamp;
    }
}
