package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Provider analytics dashboard — what the PROVIDER sees on their own dashboard.
 *
 * Contains everything from ProviderProfileStatsResponse PLUS:
 * - Detailed breakdowns (which requests got which ratings, when late, etc.)
 * - Cancellation / incomplete counts
 * - Recent ratings with full details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderAnalyticsDashboardResponse {

    private Long providerId;
    private String providerName;

    // ── Overall Rating ──
    private Double averageRating;
    private Long totalRatings;
    private String reputationLabel;
    private Map<String, ProviderProfileStatsResponse.StarDistribution> ratingDistribution;

    // ── Request Stats ──
    private Long totalAssignedRequests;
    private Long completedRequests;
    private Long cancelledRequests;
    private Double completionRate;

    // ── Latency Stats ──
    private Double onTimeRate;
    private Long lateArrivals;
    private Double averageMinutesLate;

    // ── Penalty Stats ──
    private Long incompleteServices;
    private Long cancellationPenalties;

    // ── Detailed Breakdowns (provider-only) ──

    /**
     * Recent ratings with full request details
     * Shows: what request, what star, what review, penalties
     */
    private List<RatingDetail> recentRatings;

    /**
     * Requests where provider arrived late
     * Shows: which request, how many minutes late, penalty amount
     */
    private List<LateRequestDetail> lateRequests;

    /**
     * Requests marked incomplete by customer
     * Shows: which request, reason, penalty
     */
    private List<IncompleteRequestDetail> incompleteRequests;

    // ── Inner DTOs for detailed breakdowns ──

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RatingDetail {
        private Long ratingId;
        private Long requestId;
        private String requestTitle;
        private String serviceType;
        private Integer ratingValue;
        private Double finalRating;
        private String review;
        private Boolean wasLate;
        private Integer minutesLate;
        private Boolean wasIncomplete;
        private Boolean hadCancellationPenalty;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LateRequestDetail {
        private Long requestId;
        private String requestTitle;
        private String serviceType;
        private Integer minutesLate;
        private Double lateArrivalPenalty;
        private LocalDateTime completedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncompleteRequestDetail {
        private Long requestId;
        private String requestTitle;
        private String serviceType;
        private String incompleteServiceReason;
        private Double penalty;
    }
}
