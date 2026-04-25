package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for CustomerProfile entity
 */
@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {

    /**
     * Find customer profile by user ID
     * @param userId User ID
     * @return CustomerProfile
     */
    Optional<CustomerProfile> findByUserId(Long userId);
}