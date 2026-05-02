package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * Find chat room for a specific request
     * Since 1 request = 1 chat room
     */
    Optional<ChatRoom> findByRequestId(Long requestId);

    /**
     * Get all chat rooms for a user (either as customer or provider)
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.customer.id = :userId OR cr.provider.id = :userId ORDER BY cr.createdAt DESC")
    List<ChatRoom> findUserChatRooms(@Param("userId") Long userId);

    /**
     * Get all chat rooms for a user as customer
     */
    List<ChatRoom> findByCustomerId(Long customerId);

    /**
     * Get all chat rooms for a user as provider
     */
    List<ChatRoom> findByProviderId(Long providerId);

    /**
     * Find chat between specific customer and provider for a request
     */
    Optional<ChatRoom> findByCustomerIdAndProviderIdAndRequestId(
            Long customerId, Long providerId, Long requestId);

    /**
     * Check if a user is one of the two participants in a chat room.
     */
    @Query("SELECT COUNT(cr) FROM ChatRoom cr WHERE cr.id = :roomId AND (cr.customer.id = :userId OR cr.provider.id = :userId)")
    Long countRoomMembership(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
