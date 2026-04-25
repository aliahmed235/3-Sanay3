package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.ServiceRequest;
import com.sany3.graduation_project.entites.RequestStatus;
import com.sany3.graduation_project.entites.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ServiceRequest entity
 */
@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    /**
     * Find all requests by customer
     * @param customerId Customer ID
     * @param pageable Pagination
     * @return Page of requests
     */
    Page<ServiceRequest> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Find all OPEN requests by service type
     * Used to show available requests to providers
     *
     * @param serviceType Type of service (GAS, WATER, ELECTRICITY)
     * @param pageable Pagination
     * @return Page of open requests
     */
    Page<ServiceRequest> findByServiceTypeAndStatus(ServiceType serviceType,
                                                    RequestStatus status,
                                                    Pageable pageable);

    /**
     * Find all OPEN requests near a location
     * Providers see these on their map
     *
     * @param latitude Provider's latitude
     * @param longitude Provider's longitude
     * @param radiusKm Search radius in kilometers
     * @param status Request status (usually OPEN)
     * @return List of nearby open requests
     */
    @Query(value = "SELECT sr.* FROM service_requests sr " +
            "WHERE sr.status = :status " +
            "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(sr.latitude)) * " +
            "cos(radians(sr.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(sr.latitude)))) <= :radiusKm " +
            "ORDER BY sr.created_at DESC",
            nativeQuery = true)
    List<ServiceRequest> findOpenRequestsNearby(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") Double radiusKm,
            @Param("status") String status);

    /**
     * Find open requests by service type near a location
     *
     * @param serviceType Type of service
     * @param latitude Provider's latitude
     * @param longitude Provider's longitude
     * @param radiusKm Search radius in kilometers
     * @return List of matching open requests
     */
    @Query(value = "SELECT sr.* FROM service_requests sr " +
            "WHERE sr.status = 'OPEN' " +
            "AND sr.service_type = :serviceType " +
            "AND sr.expires_at > NOW() " +
            "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(sr.latitude)) * " +
            "cos(radians(sr.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(sr.latitude)))) <= :radiusKm " +
            "ORDER BY sr.created_at DESC",
            nativeQuery = true)
    List<ServiceRequest> findOpenRequestsByServiceTypeNearby(
            @Param("serviceType") String serviceType,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") Double radiusKm);

    /**
     * Find expired OPEN requests
     * Used to auto-cancel old requests
     *
     * @return List of expired requests
     */
    @Query("SELECT sr FROM ServiceRequest sr " +
            "WHERE sr.status = 'OPEN' AND sr.expiresAt < NOW()")
    List<ServiceRequest> findExpiredOpenRequests();

    /**
     * Count OPEN requests by service type
     * @param serviceType Type of service
     * @return Number of open requests
     */
    Long countByServiceTypeAndStatus(ServiceType serviceType, RequestStatus status);
}