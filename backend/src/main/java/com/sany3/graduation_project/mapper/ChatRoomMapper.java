package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.Services.ChatService;
import com.sany3.graduation_project.dto.response.ChatRoomResponse;
import com.sany3.graduation_project.entites.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomMapper {

    private final ChatService chatService;

    /**
     * Convert entity to response DTO
     */
    public ChatRoomResponse toChatRoomResponse(ChatRoom chatRoom) {
        if (chatRoom == null) {
            return null;
        }

        var lastMessage = chatService.getLatestMessage(chatRoom.getId());
        Long messageCount = chatService.getMessageCount(chatRoom.getId());

        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .requestId(chatRoom.getRequest().getId())
                .requestTitle(chatRoom.getRequest().getTitle())
                .customerId(chatRoom.getCustomer().getId())
                .customerName(chatRoom.getCustomer().getName())
                .customerAvatar(chatRoom.getCustomer().getProfileImage())
                .providerId(chatRoom.getProvider().getId())
                .providerName(chatRoom.getProvider().getName())
                .providerAvatar(chatRoom.getProvider().getProfileImage())
                .messageCount(messageCount)
                .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}