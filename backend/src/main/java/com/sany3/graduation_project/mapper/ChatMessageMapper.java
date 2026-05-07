package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.dto.response.ChatMessageResponse;
import com.sany3.graduation_project.dto.websocket.ChatMessageDto;
import com.sany3.graduation_project.entites.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    /**
     * Convert entity to WebSocket DTO
     * Used when broadcasting to users via WebSocket
     */
    public ChatMessageDto toChatMessageDto(ChatMessage chatMessage) {
        if (chatMessage == null) {
            return null;
        }

        var sender = chatMessage.getSender();
        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .chatRoomId(chatMessage.getChatRoom().getId())
                .senderId(sender != null ? sender.getId() : null)
                .senderName(sender != null ? sender.getName() : "System")
                .senderAvatar(sender != null ? sender.getProfileImage() : null)
                .message(chatMessage.getMessage())
                .messageType(chatMessage.getMessageType())
                .latitude(chatMessage.getLatitude())
                .longitude(chatMessage.getLongitude())
                .timestamp(chatMessage.getCreatedAt())
                .build();
    }

    /**
     * Convert entity to REST response DTO
     * Used when fetching messages via HTTP
     */
    public ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage, Long currentUserId) {
        if (chatMessage == null) {
            return null;
        }

        var sender = chatMessage.getSender();
        boolean isOwn = sender != null && sender.getId().equals(currentUserId);

        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .chatRoomId(chatMessage.getChatRoom().getId())
                .senderId(sender != null ? sender.getId() : null)
                .senderName(sender != null ? sender.getName() : "System")
                .senderAvatar(sender != null ? sender.getProfileImage() : null)
                .message(chatMessage.getMessage())
                .messageType(chatMessage.getMessageType())
                .latitude(chatMessage.getLatitude())
                .longitude(chatMessage.getLongitude())
                .timestamp(chatMessage.getCreatedAt())
                .isOwn(isOwn)
                .build();
    }
}