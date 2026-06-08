package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Provider profile stats — what CUSTOMERS see when viewing a provider's profile.
 *
 * Includes:
 * - Rating distribution (percentage per star)
 * - Completion rate
 * - On-time rate
 * - Overall average + reputation label
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderProfileStatsResponse {

    private Long providerId;
    private String providerName;

    // ── Overall Rating ──
    private Double averageRating;       // e.g. 4.2
    private Long totalRatings;          // e.g. 15
    private String reputationLabel;     // e.g. "Very Good"

    /**
     * Rating distribution: star -> { count, percentage }
     * Keys: "5", "4", "3", "2", "1"
     *
     * Example:
     * { "5": { "count": 8, "percentage": 53.3 }, "4": { "count": 4, "percentage": 26.7 }, ... }
     */
    private Map<String, StarDistribution> ratingDistribution;

    // ── Performance ──
    private Double completionRate;       // e.g. 92.0 (percentage)
    private Double onTimeRate;           // e.g. 80.0 (percentage)
    private Long totalCompletedRequests; // e.g. 23

    /**
     * Inner class for each star level's count + percentage
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StarDistribution {
        private Long count;
        private Double percentage;
    }
}
