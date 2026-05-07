package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.ChatRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Override
    @EntityGraph(attributePaths = {"customer", "provider"})
    Optional<ChatRoom> findById(Long id);

    @EntityGraph(attributePaths = {"customer", "provider"})
    Optional<ChatRoom> findByCustomerIdAndProviderId(Long customerId, Long providerId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.customer.id = :userId OR cr.provider.id = :userId ORDER BY cr.createdAt DESC")
    @EntityGraph(attributePaths = {"customer", "provider"})
    List<ChatRoom> findUserChatRooms(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"customer", "provider"})
    List<ChatRoom> findByCustomerId(Long customerId);

    @EntityGraph(attributePaths = {"customer", "provider"})
    List<ChatRoom> findByProviderId(Long providerId);

    @Query("SELECT COUNT(cr) FROM ChatRoom cr WHERE cr.id = :roomId AND (cr.customer.id = :userId OR cr.provider.id = :userId)")
    Long countRoomMembership(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
