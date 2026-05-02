package com.sany3.graduation_project.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration
 * Enables real-time chat using STOMP protocol over WebSocket
 *
 * Architecture:
 * 1. Client connects to /ws endpoint
 * 2. Client subscribes to /topic/chatRoom/{roomId} (broadcast channel)
 * 3. Client sends message to /app/chat/send/{roomId}
 * 4. Server processes and broadcasts to /topic/chatRoom/{roomId}
 * 5. All connected users in room receive message instantly
 *
 * Message Flow Example:
 * Client 1 sends: /app/chat/send/1 → ChatWebSocketController.sendMessage()
 *                  → ChatService.sendTextMessage() → Save to DB
 *                  → Broadcast to /topic/chatRoom/1
 * Client 1 & 2 receive on: /topic/chatRoom/1 subscription
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketJwtChannelInterceptor webSocketJwtChannelInterceptor;

    /**
     * Configure message broker
     *
     * Simple broker: In-memory (suitable for single server)
     * For distributed systems, use RabbitMQ or Kafka
     *
     * Flow:
     * /topic/* - Broadcast to all subscribers
     * /queue/* - Private messages to specific user
     * /app/* - Application message handler prefix
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker
        // Supports /topic and /queue prefixes
        config.enableSimpleBroker("/topic", "/queue");

        // Set application destination prefix for @MessageMapping endpoints
        // Messages to /app/* are handled by controllers
        config.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix for private messages
        // Messages to /user/* are delivered to specific user
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketJwtChannelInterceptor);
    }

    /**
     * Register STOMP endpoints
     * These are the connection points for WebSocket clients
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // Main WebSocket endpoint with SockJS fallback
        // SockJS provides fallback for browsers that don't support WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*") // Allow all origins (change in production)
                .withSockJS() // Enable SockJS fallback
                .setWebSocketEnabled(true)
                .setSessionCookieNeeded(false);

        // Alternative endpoint without SockJS (for modern browsers only)
        // Use this if SockJS causes issues
        registry.addEndpoint("/ws-no-sockjs")
                .setAllowedOrigins("*");
    }
}
