package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Find latest unused, non-expired token for a user by code
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.id = :userId " +
            "AND t.code = :code AND t.used = false AND t.expiresAt > :now " +
            "ORDER BY t.createdAt DESC")
    Optional<PasswordResetToken> findValidToken(
            @Param("userId") Long userId,
            @Param("code") String code,
            @Param("now") LocalDateTime now);

    /**
     * Find by reset token (UUID) for the final password reset step
     */
    Optional<PasswordResetToken> findByResetTokenAndUsedFalse(String resetToken);

    /**
     * Count recent tokens for rate limiting (anti-abuse)
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.user.id = :userId " +
            "AND t.createdAt > :since")
    long countRecentTokensByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
