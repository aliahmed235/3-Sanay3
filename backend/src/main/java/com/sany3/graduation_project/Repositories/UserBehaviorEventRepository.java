package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.BehaviorEventType;
import com.sany3.graduation_project.entites.UserBehaviorEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserBehaviorEventRepository extends JpaRepository<UserBehaviorEvent, Long> {

    /**
     * Find users who showed intent (APP_OPENED, SERVICE_TYPE_VIEWED, SERVICE_REQUEST_STARTED)
     * but never created a SERVICE_REQUEST_CREATED event.
     * Only considers events since the given timestamp.
     * Returns distinct user IDs.
     */
    @Query("SELECT DISTINCT e.user.id FROM UserBehaviorEvent e " +
            "WHERE e.occurredAt >= :since " +
            "AND e.eventType IN ('APP_OPENED', 'SERVICE_TYPE_VIEWED', 'SERVICE_REQUEST_STARTED') " +
            "AND e.user.id NOT IN (" +
            "  SELECT DISTINCT e2.user.id FROM UserBehaviorEvent e2 " +
            "  WHERE e2.eventType = 'SERVICE_REQUEST_CREATED'" +
            ")")
    List<Long> findUsersWithIntentButNoRequest(@Param("since") LocalDateTime since);

    /**
     * Find the latest event time for a user
     */
    @Query("SELECT MAX(e.occurredAt) FROM UserBehaviorEvent e WHERE e.user.id = :userId")
    LocalDateTime findLatestEventTimeByUserId(@Param("userId") Long userId);

    /**
     * Find latest events by user and specific event types
     * Used by nudge rules to determine which rule applies
     */
    @Query("SELECT e FROM UserBehaviorEvent e " +
            "WHERE e.user.id = :userId " +
            "AND e.eventType IN :eventTypes " +
            "ORDER BY e.occurredAt DESC")
    List<UserBehaviorEvent> findLatestByUserAndTypes(
            @Param("userId") Long userId,
            @Param("eventTypes") List<BehaviorEventType> eventTypes);

    /**
     * Find events by user ID ordered by most recent
     */
    List<UserBehaviorEvent> findByUserIdOrderByOccurredAtDesc(Long userId);
}
