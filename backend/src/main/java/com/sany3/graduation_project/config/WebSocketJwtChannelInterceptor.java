package com.sany3.graduation_project.config;

import com.sany3.graduation_project.Repositories.ChatRoomRepository;
import com.sany3.graduation_project.Services.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketJwtChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CHAT_ROOM_PREFIX = "/topic/chatRoom/";
    private static final String APP_CHAT_PREFIX = "/app/chat/";

    private final JwtService jwtService;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        switch (command) {
            case CONNECT -> authenticateConnect(accessor);
            case SUBSCRIBE, SEND -> authorizeChatRoomAccess(accessor);
            default -> {
                return message;
            }
        }

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);
        if (token == null || !jwtService.validateToken(token)) {
            throw new AccessDeniedException("Invalid WebSocket token");
        }

        Long userId = jwtService.getUserIdFromToken(token);
        accessor.setUser(new WebSocketUserPrincipal(userId));
        log.debug("Authenticated WebSocket connection for user {}", userId);
    }

    private void authorizeChatRoomAccess(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal == null) {
            authenticateConnect(accessor);
            principal = accessor.getUser();
        }

        if (principal == null) {
            throw new AccessDeniedException("WebSocket user is not authenticated");
        }

        Long roomId = extractRoomId(accessor.getDestination());
        if (roomId == null) {
            return;
        }

        Long userId = parseUserId(principal);
        boolean isParticipant = chatRoomRepository.countRoomMembership(roomId, userId) > 0;
        if (!isParticipant) {
            throw new AccessDeniedException("User is not part of this chat room");
        }
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String token = firstNativeHeader(accessor, "Authorization");
        if (token == null) {
            token = firstNativeHeader(accessor, "authorization");
        }
        if (token == null) {
            token = firstNativeHeader(accessor, "token");
        }

        if (token == null || token.isBlank()) {
            return null;
        }

        token = token.trim();
        return token.startsWith(BEARER_PREFIX) ? token.substring(BEARER_PREFIX.length()) : token;
    }

    private String firstNativeHeader(StompHeaderAccessor accessor, String name) {
        List<String> values = accessor.getNativeHeader(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private Long extractRoomId(String destination) {
        if (destination == null) {
            return null;
        }

        if (destination.startsWith(CHAT_ROOM_PREFIX)) {
            return parseFirstNumber(destination.substring(CHAT_ROOM_PREFIX.length()));
        }

        if (destination.startsWith(APP_CHAT_PREFIX)) {
            String[] parts = destination.substring(APP_CHAT_PREFIX.length()).split("/");
            if (parts.length >= 2) {
                return parseFirstNumber(parts[1]);
            }
        }

        return null;
    }

    private Long parseFirstNumber(String value) {
        String firstSegment = value.split("/")[0];
        try {
            return Long.parseLong(firstSegment);
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid chat room destination");
        }
    }

    private Long parseUserId(Principal principal) {
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid WebSocket user");
        }
    }
}
