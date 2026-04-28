package com.sany3.graduation_project.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service Offer Entity
 * When a service provider responds to a customer's request with their offer
 *
 * Example:
 *   Request: "Gas problem" (Budget: 1000 PKR)
 *   Offer from Hassan: "750 PKR, 15 min" → ServiceOffer ✅
 *   Offer from Khalid: "800 PKR, 20 min" → ServiceOffer ✅
 *   Offer from Fatima: "900 PKR, 30 min" → ServiceOffer ✅
 *
 * Customer chooses the best one!
 */
@Entity
@Table(
        name = "service_offers",
        indexes = {
                @Index(name = "idx_request_id", columnList = "request_id"),
                @Index(name = "idx_provider_id", columnList = "provider_id"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_request_provider", columnList = "request_id, provider_id")
        },
        uniqueConstraints = {  // ← ADD THIS
                @UniqueConstraint(
                        name = "uk_request_provider_unique",
                        columnNames = {"request_id", "provider_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The service request this offer is for
     * Many offers can respond to ONE request
     *
     * Example: Request #456 has 3 offers
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ServiceRequest request;

    /**
     * The provider making this offer
     *
     * Example: Hassan Khan (ID: 2) is offering
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    /**
     * Price offered by provider
     * Should typically be <= customer's budget
     *
     * Example: Hassan offers 750 PKR
     *         (Customer's budget was 1000 PKR)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal offeredPrice;

    /**
     * Estimated time to arrive in minutes
     * Provider's estimate of how long they'll take to reach the customer
     *
     * Example: Hassan says "15 minutes"
     *         (He's 2.5 km away, traffic is light)
     */
    @Column(nullable = false)
    private Integer estimatedTimeMinutes;

    /**
     * Additional description/notes about the offer
     * Provider can explain why their price or time
     *
     * Example: "Master technician. Guaranteed fix in 15 min"
     *         "Using advanced equipment"
     *         "Have parts in vehicle"
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Status of this offer:
     * - PENDING: Customer hasn't responded yet
     * - ACCEPTED: Customer chose this offer! 🎉
     * - REJECTED: Customer rejected this offer
     * - WITHDRAWN: Provider took back the offer
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OfferStatus status;

    /**
     * When was this offer created?
     * Auto-set by Hibernate
     *
     * Used to show "oldest offers first" in UI
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When did customer respond to this offer?
     * Null while PENDING
     * Set when ACCEPTED or REJECTED
     *
     * Example: Offer created at 10:00, accepted at 10:30
     */
    @Column
    private LocalDateTime respondedAt;
}