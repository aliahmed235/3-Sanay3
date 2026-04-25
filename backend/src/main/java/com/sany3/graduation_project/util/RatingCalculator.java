package com.sany3.graduation_project.util;

import com.sany3.graduation_project.entites.Rating;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for calculating final rating with penalties
 *
 * Penalty System Algorithm:
 *
 * finalRating = max(1.0, customerRating - totalPenalties)
 *
 * Where penalties include:
 * 1. Cancellation after acceptance: -0.5
 * 2. Late arrival: -0.25 per 5 minutes (max -1.0)
 * 3. Incomplete service: -1.0
 */
@Slf4j
public class RatingCalculator {

    /**
     * Calculate final rating with all penalties
     *
     * @param rating Rating object with penalties
     * @return Final rating (1.0 to 5.0)
     */
    public static Double calculateFinalRating(Rating rating) {
        log.info("Calculating final rating for rating: {}", rating.getId());

        // Start with customer's rating
        double baseRating = rating.getRatingValue().doubleValue();
        log.debug("Base rating: {}", baseRating);

        // Calculate total penalty
        double totalPenalty = calculateTotalPenalty(rating);
        log.debug("Total penalty: {}", totalPenalty);

        // Apply penalty: never go below 1.0 star
        double finalRating = Math.max(1.0, baseRating - totalPenalty);

        log.info("Final rating: {} (base: {}, penalty: {})",
                finalRating, baseRating, totalPenalty);

        return Math.round(finalRating * 100.0) / 100.0;  // Round to 2 decimals
    }

    /**
     * Calculate total penalty amount
     * Sum of all applicable penalties
     *
     * @param rating Rating with penalties
     * @return Total penalty amount
     */
    public static Double calculateTotalPenalty(Rating rating) {
        double totalPenalty = 0.0;

        // Penalty 1: Cancellation
        if (rating.getCancellationPenalty() != null && rating.getCancellationPenalty()) {
            totalPenalty += 0.5;
            log.debug("Applied cancellation penalty: -0.5");
        }

        // Penalty 2: Late Arrival
        if (rating.getLateArrivalPenalty() != null && rating.getLateArrivalPenalty()) {
            double lateArrivalPenalty = calculateLateArrivalPenalty(rating.getMinutesLate());
            totalPenalty += lateArrivalPenalty;
            log.debug("Applied late arrival penalty: -{}", lateArrivalPenalty);
        }

        // Penalty 3: Incomplete Service
        if (rating.getIncompleteServicePenalty() != null && rating.getIncompleteServicePenalty()) {
            totalPenalty += 1.0;
            log.debug("Applied incomplete service penalty: -1.0");
        }

        return totalPenalty;
    }

    /**
     * Calculate late arrival penalty
     *
     * Formula: -0.25 per 5 minutes (max -1.0)
     *
     * Examples:
     *   5 min late:  -0.25
     *   10 min late: -0.50
     *   15 min late: -0.75
     *   20 min late: -1.00 (capped)
     *   30 min late: -1.00 (capped)
     *
     * @param minutesLate Minutes provider was late
     * @return Late arrival penalty (0.0 to -1.0)
     */
    public static Double calculateLateArrivalPenalty(Integer minutesLate) {
        if (minutesLate == null || minutesLate <= 0) {
            return 0.0;
        }

        // No penalty if <= 10 minutes (acceptable delay)
        if (minutesLate <= 10) {
            log.debug("Late {} minutes - within acceptable range", minutesLate);
            return 0.0;
        }

        // Penalty: -0.25 per 5 minutes, capped at -1.0
        double penalty = ((minutesLate - 10) / 5.0) * 0.25;
        double cappedPenalty = Math.min(1.0, penalty);

        log.debug("Late {} minutes - penalty: -{}", minutesLate, cappedPenalty);
        return cappedPenalty;
    }

    /**
     * Check if penalty should be applied
     *
     * @param minutesLate Minutes late from estimated time
     * @return true if penalty applies (> 10 min late)
     */
    public static boolean shouldApplyLateArrivalPenalty(Integer minutesLate) {
        return minutesLate != null && minutesLate > 10;
    }
}