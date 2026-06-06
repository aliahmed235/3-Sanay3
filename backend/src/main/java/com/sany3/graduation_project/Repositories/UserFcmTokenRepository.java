package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    /**
     * Find all active tokens for a user
     */
    List<UserFcmToken> findByUserIdAndActiveTrue(Long userId);

    /**
     * Find token by its value
     */
    Optional<UserFcmToken> findByToken(String token);

    /**
     * Deactivate all tokens for a user
     */
    @Modifying
    @Query("UPDATE UserFcmToken t SET t.active = false WHERE t.user.id = :userId")
    void deactivateAllByUserId(@Param("userId") Long userId);
}
