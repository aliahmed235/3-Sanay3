package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    @EntityGraph(attributePaths = {"request", "customer", "provider"})
    Page<Rating> findByProviderId(Long providerId, Pageable pageable);

    @EntityGraph(attributePaths = {"request", "customer", "provider"})
    Optional<Rating> findByRequestId(Long requestId);

    Long countByProviderId(Long providerId);

    // ✅ Calculate average rating for a provider
    @Query("SELECT AVG(r.finalRating) FROM Rating r WHERE r.provider.id = :providerId")
    Double calculateAverageRating(@Param("providerId") Long providerId);

    // ✅ Count cancellation penalties (Double > 0.0)
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.provider.id = :providerId AND r.cancellationPenalty > 0.0")
    Long countCancellationPenalties(@Param("providerId") Long providerId);

    // ✅ Count late arrival penalties (Double > 0.0)
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.provider.id = :providerId AND r.lateArrivalPenalty > 0.0")
    Long countLateArrivalPenalties(@Param("providerId") Long providerId);

    // ✅ Count incomplete service penalties (Double > 0.0)
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.provider.id = :providerId AND r.incompleteServicePenalty > 0.0")
    Long countIncompleteServicePenalties(@Param("providerId") Long providerId);

    // ── Rating Analytics Queries ──

    /**
     * Rating distribution: count per star value (1-5)
     * Returns List of [ratingValue, count] arrays
     */
    @Query("SELECT r.ratingValue, COUNT(r) FROM Rating r WHERE r.provider.id = :providerId GROUP BY r.ratingValue")
    List<Object[]> getRatingDistribution(@Param("providerId") Long providerId);

    /**
     * Average minutes late (only for jobs where provider was late)
     */
    @Query("SELECT AVG(r.minutesLate) FROM Rating r WHERE r.provider.id = :providerId AND r.minutesLate > 0")
    Double getAverageMinutesLate(@Param("providerId") Long providerId);

    /**
     * Count ratings where provider was late
     */
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.provider.id = :providerId AND r.minutesLate > 0")
    Long countLateRatings(@Param("providerId") Long providerId);

    /**
     * Fetch ratings with late arrivals (for provider dashboard detail)
     */
    @EntityGraph(attributePaths = {"request", "customer"})
    @Query("SELECT r FROM Rating r WHERE r.provider.id = :providerId AND r.minutesLate > 0 ORDER BY r.createdAt DESC")
    List<Rating> findLateArrivalRatings(@Param("providerId") Long providerId);

    /**
     * Fetch ratings with incomplete service (for provider dashboard detail)
     */
    @EntityGraph(attributePaths = {"request", "customer"})
    @Query("SELECT r FROM Rating r WHERE r.provider.id = :providerId AND r.incompleteServicePenalty > 0.0 ORDER BY r.createdAt DESC")
    List<Rating> findIncompleteServiceRatings(@Param("providerId") Long providerId);

    /**
     * Fetch recent ratings for provider (paginated, for dashboard)
     */
    @EntityGraph(attributePaths = {"request", "customer"})
    @Query("SELECT r FROM Rating r WHERE r.provider.id = :providerId ORDER BY r.createdAt DESC")
    List<Rating> findRecentByProviderId(@Param("providerId") Long providerId, Pageable pageable);
}