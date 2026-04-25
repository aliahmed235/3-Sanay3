package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Rating entity
 */
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Find rating for a request
     * One-to-one relationship
     *
     * @param requestId Request ID
     * @return Rating if submitted
     */
    Optional<Rating> findByRequestId(Long requestId);

    /**
     * Find all ratings for a provider
     * Used to calculate provider's reputation
     *
     * @param providerId Provider ID
     * @param pageable Pagination
     * @return Page of ratings
     */
    Page<Rating> findByProviderId(Long providerId, Pageable pageable);

    /**
     * Find all ratings by a customer
     * @param customerId Customer ID
     * @param pageable Pagination
     * @return Page of ratings
     */
    Page<Rating> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Calculate average rating for a provider
     *
     * @param providerId Provider ID
     * @return Average of finalRating field
     */
    @Query("SELECT AVG(r.finalRating) FROM Rating r WHERE r.provider.id = :providerId")
    Double calculateAverageRating(@Param("providerId") Long providerId);

    /**
     * Count ratings for a provider
     * @param providerId Provider ID
     * @return Number of ratings
     */
    Long countByProviderId(Long providerId);

    /**
     * Find ratings with cancellation penalty for a provider
     * @param providerId Provider ID
     * @return Count of cancellations
     */
    @Query("SELECT COUNT(r) FROM Rating r " +
            "WHERE r.provider.id = :providerId " +
            "AND r.cancellationPenalty = true")
    Long countCancellationPenalties(@Param("providerId") Long providerId);

    /**
     * Find ratings with late arrival penalty for a provider
     * @param providerId Provider ID
     * @return Count of late arrivals
     */
    @Query("SELECT COUNT(r) FROM Rating r " +
            "WHERE r.provider.id = :providerId " +
            "AND r.lateArrivalPenalty = true")
    Long countLateArrivalPenalties(@Param("providerId") Long providerId);

    /**
     * Find ratings with incomplete service penalty for a provider
     * @param providerId Provider ID
     * @return Count of incomplete services
     */
    @Query("SELECT COUNT(r) FROM Rating r " +
            "WHERE r.provider.id = :providerId " +
            "AND r.incompleteServicePenalty = true")
    Long countIncompleteServicePenalties(@Param("providerId") Long providerId);

    /**
     * Get provider's reliability score
     * Based on penalties and completion rate
     *
     * @param providerId Provider ID
     * @return Reliability metrics
     */
    @Query("SELECT r FROM Rating r WHERE r.provider.id = :providerId " +
            "ORDER BY r.createdAt DESC")
    Page<Rating> findRecentRatings(@Param("providerId") Long providerId, Pageable pageable);
}