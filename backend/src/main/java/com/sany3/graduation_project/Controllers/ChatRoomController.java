package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.ChatService;
import com.sany3.graduation_project.dto.request.ChatMessageRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.ChatRoomResponse;
import com.sany3.graduation_project.dto.websocket.ChatMessageDto;
import com.sany3.graduation_project.entites.ChatMessageType;
import com.sany3.graduation_project.mapper.ChatMessageMapper;
import com.sany3.graduation_project.mapper.ChatRoomMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatRoomController {

    private final ChatService chatService;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatMessageMapper chatMessageMapper;

    /**
     * Get all chat rooms for current user
     * GET /api/chats
     *
     * Returns all conversations (as customer or provider)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyChatRooms(
            Authentication authentication) {
        log.info("Fetching chat rooms for user");

        Long userId = (Long) authentication.getPrincipal();
        var chatRooms = chatService.getUserChatRooms(userId);
        var response = chatRooms.stream()
                .map(chatRoomMapper::toChatRoomResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response, "Chat rooms retrieved successfully"));
    }

    /**
     * Get chat room for a request
     * GET /api/chats/request/{requestId}
     */
    @GetMapping("/request/{requestId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoomByRequest(
            @PathVariable Long requestId,
            Authentication authentication) {
        log.info("Fetching chat room for request: {}", requestId);

        var chatRoom = chatService.getChatRoomByRequest(requestId);
        chatService.validateUserAccess(chatRoom.getId(), (Long) authentication.getPrincipal());
        var response = chatRoomMapper.toChatRoomResponse(chatRoom);

        return ResponseEntity.ok(ApiResponse.success(response, "Chat room retrieved successfully"));
    }

    /**
     * Get single chat room details
     * GET /api/chats/{chatRoomId}
     */
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoom(
            @PathVariable Long chatRoomId,
            Authentication authentication) {
        log.info("Fetching chat room: {}", chatRoomId);

        chatService.validateUserAccess(chatRoomId, (Long) authentication.getPrincipal());
        var chatRoom = chatService.getChatRoomById(chatRoomId);
        var response = chatRoomMapper.toChatRoomResponse(chatRoom);

        return ResponseEntity.ok(ApiResponse.success(response, "Chat room retrieved successfully"));
    }

    /**
     * Get messages in a chat room (paginated)
     * GET /api/chats/{chatRoomId}/messages?page=0&size=20
     *
     * Messages sorted by newest first (for infinite scroll)
     */
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageDto>>> getChatMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        log.info("Fetching messages for chat room: {}", chatRoomId);

        Long userId = (Long) authentication.getPrincipal();
        chatService.validateUserAccess(chatRoomId, userId);
        Pageable pageable = PageRequest.of(page, size);
        var messages = chatService.getChatMessages(chatRoomId, pageable);
        var response = messages.map(msg -> chatMessageMapper.toChatMessageDto(msg));

        return ResponseEntity.ok(ApiResponse.success(response, "Messages retrieved successfully"));
    }

    /**
     * Send text message (REST endpoint, also available via WebSocket)
     * POST /api/chats/{chatRoomId}/message/text
     *
     * Request Body:
     * {
     *   "message": "I'm on my way"
     * }
     */
    @PostMapping("/{chatRoomId}/message/text")
    public ResponseEntity<ApiResponse<ChatMessageDto>> sendTextMessage(
            @PathVariable Long chatRoomId,
            @RequestBody SendMessageRequest request,
            Authentication authentication) {
        log.info("Sending text message in chat room: {}", chatRoomId);

        Long senderId = (Long) authentication.getPrincipal();
        var message = chatService.sendTextMessage(chatRoomId, senderId, request.getMessage());
        var response = chatMessageMapper.toChatMessageDto(message);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Message sent successfully"));
    }

    /**
     * Send location message (REST endpoint)
     * POST /api/chats/{chatRoomId}/message/location
     *
     * Request Body:
     * {
     *   "latitude": 24.8607,
     *   "longitude": 67.0011
     * }
     */
    @PostMapping("/{chatRoomId}/message/location")
    public ResponseEntity<ApiResponse<ChatMessageDto>> sendLocationMessage(
            @PathVariable Long chatRoomId,
            @RequestBody SendLocationRequest request,
            Authentication authentication) {
        log.info("Sending location message in chat room: {}", chatRoomId);

        Long senderId = (Long) authentication.getPrincipal();
        var message = chatService.sendLocationMessage(chatRoomId, senderId,
                request.getLatitude(), request.getLongitude());
        var response = chatMessageMapper.toChatMessageDto(message);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Location sent successfully"));
    }

    /**
     * Send photo message (REST endpoint)
     * POST /api/chats/{chatRoomId}/message/photo
     *
     * Request Body:
     * {
     *   "photoUrl": "/uploads/photo_123.jpg"
     * }
     */
    @PostMapping("/{chatRoomId}/message/photo")
    public ResponseEntity<ApiResponse<ChatMessageDto>> sendPhotoMessage(
            @PathVariable Long chatRoomId,
            @RequestBody SendPhotoRequest request,
            Authentication authentication) {
        log.info("Sending photo message in chat room: {}", chatRoomId);

        Long senderId = (Long) authentication.getPrincipal();
        var message = chatService.sendPhotoMessage(chatRoomId, senderId, request.getPhotoUrl());
        var response = chatMessageMapper.toChatMessageDto(message);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Photo sent successfully"));
    }

    /**
     * Delete message (only sender can delete)
     * DELETE /api/chats/{chatRoomId}/message/{messageId}
     */
    @DeleteMapping("/{chatRoomId}/message/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long chatRoomId,
            @PathVariable Long messageId,
            Authentication authentication) {
        log.info("Deleting message: {}", messageId);

        Long userId = (Long) authentication.getPrincipal();
        chatService.validateUserAccess(chatRoomId, userId);
        chatService.deleteMessage(messageId, userId);

        return ResponseEntity.ok(ApiResponse.success(null, "Message deleted successfully"));
    }

    /**
     * Get latest message in chat room (for preview)
     * GET /api/chats/{chatRoomId}/latest-message
     */
    @GetMapping("/{chatRoomId}/latest-message")
    public ResponseEntity<ApiResponse<ChatMessageDto>> getLatestMessage(
            @PathVariable Long chatRoomId,
            Authentication authentication) {
        log.info("Fetching latest message for chat room: {}", chatRoomId);

        chatService.validateUserAccess(chatRoomId, (Long) authentication.getPrincipal());
        var message = chatService.getLatestMessage(chatRoomId);
        if (message == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No messages in this chat"));
        }

        var response = chatMessageMapper.toChatMessageDto(message);
        return ResponseEntity.ok(ApiResponse.success(response, "Latest message retrieved successfully"));
    }

    /**
     * Get message count for chat room
     * GET /api/chats/{chatRoomId}/message-count
     */
    @GetMapping("/{chatRoomId}/message-count")
    public ResponseEntity<ApiResponse<Long>> getMessageCount(
            @PathVariable Long chatRoomId,
            Authentication authentication) {
        log.info("Fetching message count for chat room: {}", chatRoomId);

        chatService.validateUserAccess(chatRoomId, (Long) authentication.getPrincipal());
        Long count = chatService.getMessageCount(chatRoomId);
        return ResponseEntity.ok(ApiResponse.success(count, "Message count retrieved successfully"));
    }

    /**
     * Helper: Send text message request
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SendMessageRequest {
        @lombok.NonNull
        private String message;
    }

    /**
     * Helper: Send location request
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SendLocationRequest {
        @lombok.NonNull
        private BigDecimal latitude;

        @lombok.NonNull
        private BigDecimal longitude;
    }

    /**
     * Helper: Send photo request
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SendPhotoRequest {
        @lombok.NonNull
        private String photoUrl;
    }
}
