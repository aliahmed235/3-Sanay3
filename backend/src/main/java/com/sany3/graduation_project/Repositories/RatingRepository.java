package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Page<Rating> findByProviderId(Long providerId, Pageable pageable);

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
}