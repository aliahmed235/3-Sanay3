package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rating Statistics for a service provider
 * Includes reputation penalties
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingStats {

    /**
     * Provider ID
     */
    private Long providerId;

    /**
     * Average rating across all ratings
     * Formula: Sum of all finalRatings / Count of ratings
     *
     * Accounts for penalties!
     * Example: 4.65 (after penalties applied)
     */
    private Double averageRating;

    /**
     * Total number of ratings received
     */
    private Long totalRatings;

    /**
     * Breakdown by stars
     */
    private Long fiveStarCount;
    private Long fourStarCount;
    private Long threeStarCount;
    private Long twoStarCount;
    private Long oneStarCount;

    /**
     * PENALTY STATISTICS
     * Track provider's problematic behavior
     */

    /**
     * Times provider accepted then cancelled
     * Example: 3 (provider cancelled 3 times)
     */
    private Long cancellationCount;

    /**
     * Times provider arrived late
     * Example: 12 (provider was late 12 times)
     */
    private Long lateArrivalCount;

    /**
     * Times service was incomplete/disputed
     * Example: 2 (customer complained service wasn't fixed)
     */
    private Long incompleteServiceCount;

    /**
     * Total penalties applied to provider
     * Formula: Sum of all penalties
     * Example: 4.75 (provider lost 4.75 stars total)
     */
    private Double totalPenaltiesApplied;

    /**
     * Cancellation rate (percentage)
     * Formula: (cancellationCount / totalRatings) * 100
     * Example: 5.77% (3 cancellations out of 52 ratings)
     */
    private Double cancellationRate;

    /**
     * Late arrival rate (percentage)
     * Formula: (lateArrivalCount / totalRatings) * 100
     * Example: 23.08% (12 late arrivals out of 52 ratings)
     */
    private Double lateArrivalRate;

    /**
     * Reliability score (0-100)
     * Based on: on-time arrivals, no cancellations, complete service
     * Formula: 100 - (penalties affecting reliability)
     * Example: 92 (very reliable)
     */
    private Double reliabilityScore;
}