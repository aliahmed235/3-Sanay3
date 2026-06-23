package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.RatingService;
import com.sany3.graduation_project.dto.request.CreateRatingRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.ProviderAnalyticsDashboardResponse;
import com.sany3.graduation_project.dto.response.ProviderProfileStatsResponse;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Slf4j
public class RatingController {

    private final RatingService ratingService;
    private final RatingMapper ratingMapper;

    @PostMapping("/request/{requestId}")
    public ResponseEntity<RatingResponse> createRating(
            @PathVariable Long requestId,
            @RequestBody CreateRatingRequest request,
            Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        log.info("POST /api/ratings/request/{} - Creating rating by customer {}", requestId, customerId);

        Rating rating = ratingService.createRating(customerId, requestId, request);
        RatingResponse response = ratingMapper.toRatingResponse(rating);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<Page<RatingResponse>> getProviderRatings(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Rating> ratings = ratingService.getProviderRatings(providerId, page, size);
        Page<RatingResponse> response = ratings.map(ratingMapper::toRatingResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<ApiResponse<RatingResponse>> getRatingForRequest(
            @PathVariable Long requestId) {
        try {
            log.info("Fetching rating for request: {}", requestId);
            if (requestId == null || requestId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid request ID"));
            }

            var rating = ratingService.getRatingForRequest(requestId);
            var response = ratingMapper.toRatingResponse(rating);

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Rating retrieved"));

        } catch (ResourceNotFoundException e) {
            log.warn("Rating not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching rating: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/provider/{providerId}/average")
    public ResponseEntity<Double> getProviderAverageRating(
            @PathVariable Long providerId) {
        log.info("GET /api/ratings/provider/{}/average - Fetching average rating", providerId);

        Double avgRating = ratingService.getProviderAverageRating(providerId);
        return ResponseEntity.ok(avgRating != null ? avgRating : 0.0);
    }

    @GetMapping("/provider/{providerId}/count")
    public ResponseEntity<Long> getProviderRatingCount(
            @PathVariable Long providerId) {
        log.info("GET /api/ratings/provider/{}/count - Fetching rating count", providerId);

        Long count = ratingService.getProviderRatingCount(providerId);
        return ResponseEntity.ok(count != null ? count : 0L);
    }
    @GetMapping("/provider/{providerId}/stats")
    public ResponseEntity<ProviderRatingStats> getProviderStats(
            @PathVariable Long providerId) {
        log.info("GET /api/ratings/provider/{}/stats - Fetching provider stats", providerId);

        ProviderRatingStats stats = ratingService.getProviderStats(providerId);
        return ResponseEntity.ok(stats);
    }
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
    @GetMapping("/provider/{providerId}/profile-stats")
    public ResponseEntity<ApiResponse<ProviderProfileStatsResponse>> getProviderProfileStats(
            @PathVariable Long providerId) {
        log.info("GET /api/ratings/provider/{}/profile-stats", providerId);

        try {
            ProviderProfileStatsResponse stats = ratingService.getProviderProfileStats(providerId);
            return ResponseEntity.ok(ApiResponse.success(stats, "Provider profile stats retrieved"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    @GetMapping("/provider/my-analytics")
    public ResponseEntity<ApiResponse<ProviderAnalyticsDashboardResponse>> getProviderAnalyticsDashboard(
            Authentication authentication) {
        Long providerId = (Long) authentication.getPrincipal();
        log.info("GET /api/ratings/provider/my-analytics - Provider: {}", providerId);

        try {
            ProviderAnalyticsDashboardResponse dashboard = ratingService.getProviderAnalyticsDashboard(providerId);
            return ResponseEntity.ok(ApiResponse.success(dashboard, "Provider analytics retrieved"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class PenaltyStats {
        private Long providerId;
        private Long cancellationPenalties;
        private Long lateArrivalPenalties;
        private Long incompleteServicePenalties;
    }
}