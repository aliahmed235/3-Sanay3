package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.RequestStatus;
import com.sany3.graduation_project.entites.ServiceRequest;
import com.sany3.graduation_project.entites.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    @Override
    @EntityGraph(attributePaths = {"customer", "acceptedProvider", "rating", "chatRoom"})
    java.util.Optional<ServiceRequest> findById(Long id);

    /**
     * Get all requests by a customer
     */
    @EntityGraph(attributePaths = {"customer", "acceptedProvider", "rating"})
    Page<ServiceRequest> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Get open requests (available for providers)
     */
    List<ServiceRequest> findByStatus(RequestStatus status);

    /**
     * Get requests by service type
     */
    @EntityGraph(attributePaths = {"customer", "acceptedProvider", "rating"})
    Page<ServiceRequest> findByServiceType(ServiceType serviceType, Pageable pageable);

    /**
     * Get requests by service type and status
     * Excludes requests the provider already offered on
     */
    @EntityGraph(attributePaths = {"customer", "acceptedProvider", "rating"})
    @Query("SELECT sr FROM ServiceRequest sr " +
            "WHERE sr.serviceType = :serviceType " +
            "AND sr.status = :status " +
            "AND sr.id NOT IN (SELECT so.request.id FROM ServiceOffer so WHERE so.provider.id = :providerId) " +
            "ORDER BY sr.createdAt DESC")
    Page<ServiceRequest> findByServiceTypeAndStatusExcludingOffered(
            @Param("serviceType") ServiceType serviceType,
            @Param("status") RequestStatus status,
            @Param("providerId") Long providerId,
            Pageable pageable);

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
     * Excludes requests the provider already offered on
     */
    @EntityGraph(attributePaths = {"customer", "acceptedProvider", "rating"})
    @Query("SELECT sr FROM ServiceRequest sr " +
            "WHERE sr.serviceType = :serviceType " +
            "AND sr.status = 'OPEN' " +
            "AND sr.id NOT IN (SELECT so.request.id FROM ServiceOffer so WHERE so.provider.id = :providerId) " +
            "AND SQRT(POWER(sr.latitude - :latitude, 2) + POWER(sr.longitude - :longitude, 2)) * 111 <= :radiusKm " +
            "ORDER BY sr.createdAt DESC")
    List<ServiceRequest> findOpenRequestsByServiceTypeNearby(
            @Param("serviceType") ServiceType serviceType,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") Double radiusKm,
            @Param("providerId") Long providerId);

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

    /**
     * Provider schedule: all requests for a specific date (includes all statuses)
     * Checks both scheduledAt and acceptedAt for the date range
     */
    @EntityGraph(attributePaths = {"customer", "acceptedProvider"})
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.acceptedProvider.id = :providerId " +
            "AND sr.status IN ('ACCEPTED', 'ONGOING', 'COMPLETED', 'CANCELLED') " +
            "AND (sr.scheduledAt BETWEEN :startOfDay AND :endOfDay " +
            "OR (sr.scheduledAt IS NULL AND sr.acceptedAt BETWEEN :startOfDay AND :endOfDay)) " +
            "ORDER BY COALESCE(sr.scheduledAt, sr.acceptedAt) ASC")
    List<ServiceRequest> findProviderSchedule(
            @Param("providerId") Long providerId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Provider upcoming jobs: all active requests (accepted/ongoing) from now onwards
     */
    @EntityGraph(attributePaths = {"customer", "acceptedProvider"})
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.acceptedProvider.id = :providerId " +
            "AND sr.status IN ('ACCEPTED', 'ONGOING') " +
            "ORDER BY COALESCE(sr.scheduledAt, sr.acceptedAt) ASC")
    List<ServiceRequest> findProviderUpcomingJobs(@Param("providerId") Long providerId);

    /**
     * Provider all jobs: includes completed and cancelled (for schedule history)
     */
    @EntityGraph(attributePaths = {"customer", "acceptedProvider"})
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.acceptedProvider.id = :providerId " +
            "AND sr.status IN ('ACCEPTED', 'ONGOING', 'COMPLETED', 'CANCELLED') " +
            "ORDER BY COALESCE(sr.scheduledAt, sr.acceptedAt) ASC")
    List<ServiceRequest> findProviderAllJobs(@Param("providerId") Long providerId);

    /**
     * Get dates that have jobs for a provider (for calendar view)
     * Returns dates for all statuses so provider sees full history
     */
    @Query("SELECT CAST(COALESCE(sr.scheduledAt, sr.acceptedAt) AS LocalDate) " +
            "FROM ServiceRequest sr WHERE sr.acceptedProvider.id = :providerId " +
            "AND sr.status IN ('ACCEPTED', 'ONGOING', 'COMPLETED', 'CANCELLED') " +
            "GROUP BY CAST(COALESCE(sr.scheduledAt, sr.acceptedAt) AS LocalDate) " +
            "ORDER BY CAST(COALESCE(sr.scheduledAt, sr.acceptedAt) AS LocalDate) ASC")
    List<LocalDate> findProviderJobDates(@Param("providerId") Long providerId);

    /**
     * Check if provider has a conflicting job at a given time
     * A conflict = provider has an accepted/ongoing job whose time range overlaps
     * Time range = scheduledAt to scheduledAt + estimatedTimeMinutes (from accepted offer)
     */
    @Query("SELECT sr FROM ServiceRequest sr JOIN ServiceOffer so ON so.request.id = sr.id " +
            "WHERE sr.acceptedProvider.id = :providerId " +
            "AND sr.status IN ('ACCEPTED', 'ONGOING') " +
            "AND so.provider.id = :providerId AND so.status = 'ACCEPTED' " +
            "AND sr.scheduledAt IS NOT NULL " +
            "AND :requestStart < FUNCTION('TIMESTAMPADD', MINUTE, so.estimatedTimeMinutes, sr.scheduledAt) " +
            "AND :requestEnd > sr.scheduledAt")
    List<ServiceRequest> findConflictingJobs(
            @Param("providerId") Long providerId,
            @Param("requestStart") LocalDateTime requestStart,
            @Param("requestEnd") LocalDateTime requestEnd);

    /**
     * Provider portfolio: completed requests that have a work summary
     */
    @EntityGraph(attributePaths = {"customer", "acceptedProvider", "workPhotos"})
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.acceptedProvider.id = :providerId " +
            "AND sr.status = 'COMPLETED' AND sr.workSummary IS NOT NULL " +
            "ORDER BY sr.completedAt DESC")
    List<ServiceRequest> findProviderCompletedWithWorkSummary(@Param("providerId") Long providerId);
}
