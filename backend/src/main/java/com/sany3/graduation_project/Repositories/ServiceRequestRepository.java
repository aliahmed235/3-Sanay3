package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.RequestStatus;
import com.sany3.graduation_project.entites.ServiceRequest;
import com.sany3.graduation_project.entites.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    /**
     * Get all requests by a customer
     */
    Page<ServiceRequest> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Get open requests (available for providers)
     */
    List<ServiceRequest> findByStatus(RequestStatus status);

    /**
     * Get requests by service type
     */
    Page<ServiceRequest> findByServiceType(ServiceType serviceType, Pageable pageable);

    /**
     * Get requests by service type and status
     * Used to show providers open work in their category
     */
    Page<ServiceRequest> findByServiceTypeAndStatus(ServiceType serviceType, RequestStatus status, Pageable pageable);

    /**
     * Get active requests (OPEN, ACCEPTED)
     */
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.status IN ('OPEN', 'ACCEPTED') ORDER BY sr.createdAt DESC")
    List<ServiceRequest> findActiveRequests();

    /**
     * Get requests accepted by a provider
     */
    List<ServiceRequest> findByAcceptedProviderId(Long providerId);

    /**
     * Get completed requests for a provider (for ratings)
     */
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.acceptedProvider.id = :providerId AND sr.status = 'COMPLETED'")
    List<ServiceRequest> findCompletedRequestsByProvider(@Param("providerId") Long providerId);

    /**
     * Find open requests near a location (within radius)
     * Used for map filtering
     * Calculation: distance in km = SQRT(lat_diff^2 + lon_diff^2) * 111
     */

    @Query(value = "SELECT sr.* FROM service_requests sr WHERE " +
            "SQRT(POW(sr.latitude - :latitude, 2) + POW(sr.longitude - :longitude, 2)) * 111 <= :radiusKm " +
            "AND sr.status = 'OPEN'", nativeQuery = true)
    List<ServiceRequest> findRequestsNearby(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") Double radiusKm);

    /**
     * Find open requests by service type and nearby location
     * For provider map view (filtered by service type + distance)
     */
    @Query(value = "SELECT sr.* FROM service_requests sr " +
            "WHERE sr.service_type = :serviceType " +
            "AND sr.status = 'OPEN' " +
            "AND SQRT(POW(sr.latitude - :latitude, 2) + POW(sr.longitude - :longitude, 2)) * 111 <= :radiusKm " +
            "ORDER BY sr.created_at DESC",
            nativeQuery = true)
    List<ServiceRequest> findOpenRequestsByServiceTypeNearby(
            @Param("serviceType") String serviceType,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") Double radiusKm);

    /**
     * Find expired OPEN requests
     * Used by scheduled task to auto-cancel old requests
     */
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.status = 'OPEN' AND sr.expiresAt < CURRENT_TIMESTAMP")
    List<ServiceRequest> findExpiredOpenRequests();

    /**
     * Check if customer has active requests
     */
    Long countByCustomerIdAndStatusIn(Long customerId, List<RequestStatus> statuses);
}
