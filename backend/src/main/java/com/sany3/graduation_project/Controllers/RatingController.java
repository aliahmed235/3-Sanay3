package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.RatingService;
import com.sany3.graduation_project.dto.request.CreateRatingRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.ProviderRatingStats;
import com.sany3.graduation_project.dto.response.RatingResponse;
import com.sany3.graduation_project.entites.Rating;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import com.sany3.graduation_project.mapper.RatingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Slf4j
public class RatingController {

    private final RatingService ratingService;
    private final RatingMapper ratingMapper;

    /**
     * Create a new rating for a completed service
     * POST /api/ratings/request/{requestId}
     */
    @PostMapping("/request/{requestId}")
    public ResponseEntity<RatingResponse> createRating(
            @PathVariable Long requestId,
            @RequestBody CreateRatingRequest request,
            @RequestHeader("X-User-Id") Long customerId) {
        log.info("POST /api/ratings/request/{} - Creating rating", requestId);

        Rating rating = ratingService.createRating(customerId, requestId, request);
        RatingResponse response = ratingMapper.toRatingResponse(rating);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all ratings for a provider
     * GET /api/ratings/provider/{providerId}?page=0&size=10
     */
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<Page<RatingResponse>>> getProviderRatings(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            log.info("GET /api/ratings/provider/{} - Fetching ratings (page: {}, size: {})",
                    providerId, page, size);

            // ✅ Create pageable object
            Pageable pageable = PageRequest.of(page, size);

            // ✅ Call service (all business logic happens here)
            var ratings = ratingService.getProviderRatings(providerId, pageable);

            // ✅ Map to response
            var responses = ratings.map(ratingMapper::toRatingResponse);

            // ✅ Return success
            return ResponseEntity.ok(
                    ApiResponse.success(responses, "Ratings retrieved successfully"));

        } catch (ResourceNotFoundException e) {
            log.error("Provider not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error fetching ratings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching ratings"));
        }
    }

    /**
     * Get provider average rating
     * GET /api/ratings/provider/{providerId}/average
     */
    @GetMapping("/provider/{providerId}/average")
    public ResponseEntity<Double> getProviderAverageRating(
            @PathVariable Long providerId) {
        log.info("GET /api/ratings/provider/{}/average - Fetching average rating", providerId);

        Double avgRating = ratingService.getProviderAverageRating(providerId);
        return ResponseEntity.ok(avgRating != null ? avgRating : 0.0);
    }

    /**
     * Get provider rating count
     * GET /api/ratings/provider/{providerId}/count
     */
    @GetMapping("/provider/{providerId}/count")
    public ResponseEntity<Long> getProviderRatingCount(
            @PathVariable Long providerId) {
        log.info("GET /api/ratings/provider/{}/count - Fetching rating count", providerId);

        Long count = ratingService.getProviderRatingCount(providerId);
        return ResponseEntity.ok(count != null ? count : 0L);
    }

    /**
     * Get provider statistics (average, count, earnings)
     * GET /api/ratings/provider/{providerId}/stats
     */
    @GetMapping("/provider/{providerId}/stats")
    public ResponseEntity<ProviderRatingStats> getProviderStats(
            @PathVariable Long providerId) {
        log.info("GET /api/ratings/provider/{}/stats - Fetching provider stats", providerId);

        ProviderRatingStats stats = ratingService.getProviderStats(providerId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get penalty counts for provider (transparency)
     * GET /api/ratings/provider/{providerId}/penalties
     */
    @GetMapping("/provider/{providerId}/penalties")
    public ResponseEntity<PenaltyStats> getPenaltyStats(
            @PathVariable Long providerId) {
        log.info("GET /api/ratings/provider/{}/penalties - Fetching penalty stats", providerId);

        PenaltyStats stats = PenaltyStats.builder()
                .providerId(providerId)
                .cancellationPenalties(ratingService.getCancellationPenaltyCount(providerId))
                .lateArrivalPenalties(ratingService.getLateArrivalPenaltyCount(providerId))
                .incompleteServicePenalties(ratingService.getIncompleteServicePenaltyCount(providerId))
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * Inner class for penalty statistics response
     */
    @lombok.Data
    @lombok.Builder
    public static class PenaltyStats {
        private Long providerId;
        private Long cancellationPenalties;
        private Long lateArrivalPenalties;
        private Long incompleteServicePenalties;
    }
}