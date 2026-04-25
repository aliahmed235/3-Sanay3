package com.sany3.graduation_project.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Rating Entity with Reputation Penalty System
 *
 * Reputation Penalties:
 * 1. CANCELLATION_AFTER_ACCEPTANCE: Provider accepted then cancelled (-0.5 stars)
 * 2. LATE_ARRIVAL: Provider arrived late after accepting (-0.25 stars)
 * 3. INCOMPLETE_SERVICE: Provider didn't complete service (-1.0 stars)
 *
 * Example:
 *   Customer rates: 5 stars
 *   Provider was 15 min late: -0.25
 *   Final rating: 4.75 stars
 */
@Entity
@Table(name = "ratings", indexes = {
        @Index(name = "idx_request_id", columnList = "request_id"),
        @Index(name = "idx_provider_id", columnList = "provider_id"),
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_rating_value", columnList = "rating_value"),
        @Index(name = "idx_final_rating", columnList = "final_rating"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The service request being rated
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", unique = true, nullable = false)
    private ServiceRequest request;

    /**
     * Customer who is rating
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    /**
     * Provider being rated
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    /**
     * Customer's initial rating (1-5 stars)
     * Before any penalties applied
     *
     * Example: 5 (customer's raw rating)
     */
    @Column(nullable = false)
    private Integer ratingValue;

    /**
     * Written review from customer
     */
    @Column(columnDefinition = "TEXT", length = 500)
    private String review;

    /**
     * PENALTY 1: Cancellation After Acceptance
     * Applied when: Provider accepted offer then cancelled
     * Penalty: -0.5 stars
     *
     * True = penalty applied
     * False = no penalty
     */
    @Column(nullable = false)
    private Boolean cancellationPenalty;

    /**
     * PENALTY 2: Late Arrival
     * Applied when: Provider arrived > 10 minutes late from estimated time
     * Penalty: -0.25 stars per 5 minutes late (max -1.0)
     *
     * Example:
     *   Estimated: 10:00
     *   Arrived: 10:15 (15 min late)
     *   Penalty: -0.75 stars (3 × 0.25)
     *
     * Formula: Math.min(1.0, (minutesLate / 5) * 0.25)
     */
    @Column(nullable = false)
    private Boolean lateArrivalPenalty;

    /**
     * Minutes late that triggered penalty
     * Null if no penalty
     *
     * Example: 15 (provider was 15 minutes late)
     */
    @Column
    private Integer minutesLate;

    /**
     * PENALTY 3: Incomplete Service
     * Applied when: Provider marked complete but customer disputes
     * Penalty: -1.0 stars
     *
     * True = penalty applied
     * False = no penalty
     */
    @Column(nullable = false)
    private Boolean incompleteServicePenalty;

    /**
     * Reason for incomplete service
     * Filled by customer if disputing completion
     *
     * Example: "Gas is still not working"
     *         "Provider didn't fix the problem"
     */
    @Column(columnDefinition = "TEXT", length = 500)
    private String incompleteServiceReason;

    /**
     * Total penalty amount (sum of all penalties)
     * Calculated automatically
     *
     * Formula:
     *   totalPenalty = (cancellationPenalty ? 0.5 : 0) +
     *                  (lateArrivalPenalty ? lateArrivalAmount : 0) +
     *                  (incompleteServicePenalty ? 1.0 : 0)
     *
     * Max penalty: 2.75 (all three penalties)
     * Example: 0.75 (only late arrival penalty)
     */
    @Column(precision = 3, scale = 2)
    private Double totalPenalty;

    /**
     * Final rating after all penalties applied
     * Formula: max(1.0, ratingValue - totalPenalty)
     * Minimum rating: 1 star (never goes below)
     *
     * Example:
     *   ratingValue: 5 stars
     *   totalPenalty: 0.75
     *   finalRating: 4.25 stars ⭐⭐⭐⭐
     */
    @Column(precision = 3, scale = 2)
    private Double finalRating;

    /**
     * When was this rating submitted?
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}