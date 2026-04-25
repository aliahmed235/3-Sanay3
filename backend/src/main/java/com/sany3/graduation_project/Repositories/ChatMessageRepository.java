package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ChatMessage entity
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Find all messages in a chat room
     * @param chatRoomId Chat room ID
     * @param pageable Pagination (for loading old messages)
     * @return Page of messages
     */
    Page<ChatMessage> findByChatRoomId(Long chatRoomId, Pageable pageable);

    /**
     * Find all messages in a chat room
     * @param chatRoomId Chat room ID
     * @return List of messages (sorted by date)
     */
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    /**
     * Find recent messages (last N messages)
     * @param chatRoomId Chat room ID
     * @param pageable Pagination
     * @return Latest messages
     */
    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);

    /**
     * Find messages by sender
     * @param senderId Sender ID
     * @param pageable Pagination
     * @return Page of messages
     */
    Page<ChatMessage> findBySenderId(Long senderId, Pageable pageable);

    /**
     * Find all location sharing messages in a chat room
     * @param chatRoomId Chat room ID
     * @return List of location messages
     */
    List<ChatMessage> findByChatRoomIdAndMessageTypeOrderByCreatedAtDesc(
            Long chatRoomId, String messageType);
}