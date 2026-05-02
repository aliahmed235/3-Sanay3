package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.ChatMessageRepository;
import com.sany3.graduation_project.Repositories.ChatRoomRepository;
import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.entites.ChatMessage;
import com.sany3.graduation_project.entites.ChatMessageType;
import com.sany3.graduation_project.entites.ChatRoom;
import com.sany3.graduation_project.entites.User;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    /**
     * Get chat room for a request
     *
     * @param requestId Request ID
     * @return Chat room
     */
    public ChatRoom getChatRoomByRequest(Long requestId) {
        log.debug("Fetching chat room for request: {}", requestId);
        return chatRoomRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));
    }

    /**
     * Get chat room by ID
     *
     * @param chatRoomId Chat room ID
     * @return Chat room
     */
    public ChatRoom getChatRoomById(Long chatRoomId) {
        log.debug("Fetching chat room: {}", chatRoomId);
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));
    }

    public void validateUserAccess(Long chatRoomId, Long userId) {
        if (chatRoomRepository.countRoomMembership(chatRoomId, userId) == 0) {
            throw new IllegalArgumentException("User is not part of this chat room");
        }
    }

    /**
     * Get all chat rooms for a user
     * Shows list of all conversations (customer can chat with multiple providers)
     *
     * @param userId User ID
     * @return List of chat rooms
     */
    public List<ChatRoom> getUserChatRooms(Long userId) {
        log.debug("Fetching chat rooms for user: {}", userId);
        return chatRoomRepository.findUserChatRooms(userId);
    }

    /**
     * Get messages in a chat room (paginated, newest first)
     * Load 20 messages at a time for performance
     *
     * @param chatRoomId Chat room ID
     * @param pageable Pagination
     * @return Page of messages
     */
    public Page<ChatMessage> getChatMessages(Long chatRoomId, Pageable pageable) {
        log.debug("Fetching messages for chat room: {}", chatRoomId);
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);
    }

    /**
     * Get all messages in a chat room (not paginated)
     * For initial load or export
     *
     * @param chatRoomId Chat room ID
     * @return List of messages (chronological order)
     */
    public List<ChatMessage> getAllChatMessages(Long chatRoomId) {
        log.debug("Fetching all messages for chat room: {}", chatRoomId);
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);
    }

    /**
     * Send text message
     *
     * Validates:
     * - Chat room exists
     * - Sender exists
     * - Sender is part of chat room
     *
     * @param chatRoomId Chat room ID
     * @param senderId Sender ID
     * @param message Message text
     * @return Saved message
     */
    public ChatMessage sendTextMessage(Long chatRoomId, Long senderId, String message) {
        log.info("Sending text message in chat room: {}", chatRoomId);

        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate sender is part of chat room
        if (!isUserInChatRoom(chatRoom, senderId)) {
            throw new IllegalArgumentException("User is not part of this chat room");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(message)
                .messageType(ChatMessageType.TEXT)
                .build();

        chatMessage = chatMessageRepository.save(chatMessage);
        log.info("Text message saved with ID: {}", chatMessage.getId());

        return chatMessage;
    }

    /**
     * Send location message
     * Provider shares their current GPS coordinates
     *
     * Validates:
     * - Chat room exists
     * - Sender exists
     * - Sender is part of chat room
     *
     * @param chatRoomId Chat room ID
     * @param senderId Sender ID
     * @param latitude Provider's latitude
     * @param longitude Provider's longitude
     * @return Saved message
     */
    public ChatMessage sendLocationMessage(Long chatRoomId, Long senderId,
                                           BigDecimal latitude, BigDecimal longitude) {
        log.info("Sending location message in chat room: {}", chatRoomId);

        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!isUserInChatRoom(chatRoom, senderId)) {
            throw new IllegalArgumentException("User is not part of this chat room");
        }

        String locationText = latitude + "," + longitude;

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(locationText)
                .messageType(ChatMessageType.LOCATION)
                .latitude(latitude)
                .longitude(longitude)
                .build();

        chatMessage = chatMessageRepository.save(chatMessage);
        log.info("Location message saved with ID: {}", chatMessage.getId());

        return chatMessage;
    }

    /**
     * Send photo message
     *
     * Validates:
     * - Chat room exists
     * - Sender exists
     * - Sender is part of chat room
     *
     * @param chatRoomId Chat room ID
     * @param senderId Sender ID
     * @param photoUrl Photo URL (from file upload service)
     * @return Saved message
     */
    public ChatMessage sendPhotoMessage(Long chatRoomId, Long senderId, String photoUrl) {
        log.info("Sending photo message in chat room: {}", chatRoomId);

        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!isUserInChatRoom(chatRoom, senderId)) {
            throw new IllegalArgumentException("User is not part of this chat room");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(photoUrl)
                .messageType(ChatMessageType.PHOTO)
                .build();

        chatMessage = chatMessageRepository.save(chatMessage);
        log.info("Photo message saved with ID: {}", chatMessage.getId());

        return chatMessage;
    }

    /**
     * Get latest message in a chat room (for preview in list)
     *
     * @param chatRoomId Chat room ID
     * @return Latest message or null
     */
    public ChatMessage getLatestMessage(Long chatRoomId) {
        log.debug("Fetching latest message for chat room: {}", chatRoomId);
        return chatMessageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
                .orElse(null);
    }

    /**
     * Get message count for a chat room
     *
     * @param chatRoomId Chat room ID
     * @return Message count
     */
    public Long getMessageCount(Long chatRoomId) {
        log.debug("Counting messages for chat room: {}", chatRoomId);
        return chatMessageRepository.countByChatRoomId(chatRoomId);
    }

    /**
     * Delete a message (only sender can delete)
     *
     * @param messageId Message ID
     * @param userId User ID (sender)
     */
    public void deleteMessage(Long messageId, Long userId) {
        log.info("Deleting message: {}", messageId);

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Validate sender
        if (!message.getSender().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own messages");
        }

        chatMessageRepository.delete(message);
        log.info("Message deleted: {}", messageId);
    }

    /**
     * Helper: Check if user is part of chat room
     */
    private boolean isUserInChatRoom(ChatRoom chatRoom, Long userId) {
        return chatRoom.getCustomer().getId().equals(userId) ||
                chatRoom.getProvider().getId().equals(userId);
    }

    /**
     * Get messages from specific sender in a chat room
     *
     * @param chatRoomId Chat room ID
     * @param senderId Sender ID
     * @return List of messages
     */
    public List<ChatMessage> getMessagesBySender(Long chatRoomId, Long senderId) {
        log.debug("Fetching messages from sender: {} in chat room: {}", senderId, chatRoomId);
        return chatMessageRepository.findByChatRoomIdAndSenderId(chatRoomId, senderId);
    }
}
