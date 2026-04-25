package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.ServiceProviderProfile;
import com.sany3.graduation_project.entites.ServiceType;
import com.sany3.graduation_project.entites.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ServiceProviderProfile entity
 */
@Repository
public interface ServiceProviderProfileRepository extends JpaRepository<ServiceProviderProfile, Long> {

    /**
     * Find provider profile by user ID
     * @param userId User ID
     * @return ServiceProviderProfile
     */
    Optional<ServiceProviderProfile> findByUserId(Long userId);

    /**
     * Find all providers by service type
     * @param serviceType Type of service
     * @param pageable Pagination
     * @return Page of providers
     */
    Page<ServiceProviderProfile> findByServiceType(ServiceType serviceType, Pageable pageable);

    /**
     * Find all VERIFIED providers by service type
     * @param serviceType Type of service
     * @param pageable Pagination
     * @return Page of verified providers
     */
    Page<ServiceProviderProfile> findByServiceTypeAndIsVerified(
            ServiceType serviceType,
            Boolean isVerified,
            Pageable pageable);

    /**
     * Find all providers with pending verification
     * @param pageable Pagination
     * @return Page of pending providers
     */
    Page<ServiceProviderProfile> findByVerificationStatus(
            VerificationStatus status,
            Pageable pageable);

    /**
     * Count verified providers by service type
     * @param serviceType Type of service
     * @return Number of verified providers
     */
    Long countByServiceTypeAndIsVerified(ServiceType serviceType, Boolean isVerified);
}