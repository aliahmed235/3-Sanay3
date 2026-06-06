package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /**
     * Check if a notification of a given type was already sent to a user
     * Used for anti-spam: one nudge per user
     */
    boolean existsByUserIdAndNotificationType(Long userId, String notificationType);

    /**
     * Get notification history for a user
     */
    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
