package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Get all messages in a chat room
     * Paginated for performance
     *
     * Example: Get last 20 messages
     */
    @EntityGraph(attributePaths = {"chatRoom", "sender"})
    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);

    /**
     * Get messages from a chat room in chronological order
     * For initial load
     */
    @EntityGraph(attributePaths = {"chatRoom", "sender"})
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    /**
     * Count total messages in a chat room
     */
    Long countByChatRoomId(Long chatRoomId);

    /**
     * Get latest message in a chat room (for preview)
     */
    @EntityGraph(attributePaths = {"chatRoom", "sender"})
    Optional<ChatMessage> findFirstByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);

    /**
     * Get all messages sent by a specific user
     */
    @EntityGraph(attributePaths = {"chatRoom", "sender"})
    List<ChatMessage> findBySenderId(Long senderId);

    /**
     * Get messages in a chat room from a specific sender
     */
    @EntityGraph(attributePaths = {"chatRoom", "sender"})
    List<ChatMessage> findByChatRoomIdAndSenderId(Long chatRoomId, Long senderId);
}
