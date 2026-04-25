package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ChatRoom entity
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * Find chat room by request ID
     * One-to-one relationship
     *
     * @param requestId Request ID
     * @return ChatRoom if exists
     */
    Optional<ChatRoom> findByRequestId(Long requestId);

    /**
     * Find all chat rooms for a user (either as customer or provider)
     * @param customerId User ID
     * @param pageable Pagination
     * @return Page of chat rooms
     */
    Page<ChatRoom> findByCustomerIdOrProviderId(Long customerId,
                                                Long providerId,
                                                Pageable pageable);

    /**
     * Find chat room between specific customer and provider
     * @param customerId Customer ID
     * @param providerId Provider ID
     * @return ChatRoom if exists
     */
    Optional<ChatRoom> findByCustomerIdAndProviderId(Long customerId, Long providerId);
}