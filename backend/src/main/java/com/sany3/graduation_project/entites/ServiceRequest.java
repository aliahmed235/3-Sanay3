package com.sany3.graduation_project.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service Request Entity
 * Represents a customer's service request
 *
 * Example:
 *   Customer: "I have a gas problem"
 *   Location: [24.7898, 67.0345] (GPS coordinates)
 *   Status: OPEN (waiting for provider offers)
 */
@Entity
@Table(name = "service_requests", indexes = {
        @Index(name = "idx_service_type_status", columnList = "service_type, status"),
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_expires_at", columnList = "expires_at"),
        @Index(name = "idx_location", columnList = "latitude, longitude")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Customer who submitted the request
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    /**
     * Service Type: GAS, WATER, ELECTRICITY
     * Used to filter which providers see this request
     *
     * Example: GAS → Only GAS providers see this request
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    /**
     * Request title/summary
     * Example: "Gas connection not working"
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * Detailed description of the problem
     * Example: "No gas in kitchen. Need urgent fix."
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Customer's address
     * Example: "House #123, Clifton, Karachi"
     */
    @Column(length = 255)
    private String address;

    /**
     * Latitude of service location 📍
     * Range: -90 to 90
     * Example: 24.7898
     */
    @Column(columnDefinition = "DECIMAL(10, 8)", nullable = false)
    private BigDecimal latitude;

    @Column(columnDefinition = "DECIMAL(10, 8)", nullable = false)
    private BigDecimal longitude;


    /**
     * Budget - what customer is willing to pay
     * Optional: customer might not set this
     * Example: 1000 PKR
     */

    /**
     * Request Status:
     * - OPEN: Waiting for offers
     * - ACCEPTED: Customer accepted an offer
     * - ONGOING: Service in progress (after QR scan #1)
     * - COMPLETED: Service finished (after QR scan #2)
     * - CANCELLED: Customer cancelled
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    /**
     * Provider who accepted this request
     * Null until customer accepts an offer
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_provider_id")
    private User acceptedProvider;

    /**
     * When was the offer accepted?
     */
    @Column
    private LocalDateTime acceptedAt;

    /**
     * When did service start? (After QR code #1 scan)
     */
    @Column
    private LocalDateTime startedAt;

    /**
     * When did service complete? (After QR code #2 scan)
     */
    @Column
    private LocalDateTime completedAt;

    /**
     * Timestamp when request was created
     * Auto-set by Hibernate
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when request was last updated
     * Auto-updated by Hibernate
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * When does this request expire?
     * Requests expire after 24 hours if no one accepts
     * Auto-calculated in service layer
     *
     * Example: Created at 2026-04-24 10:00
     *         Expires at 2026-04-25 10:00
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * All offers for this request
     * One-to-many: 1 request → multiple offers
     */
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOffer> offers;

    /**
     * Chat room for this request
     * One-to-one: 1 request → 1 chat room (after acceptance)
     */
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private ChatRoom chatRoom;

    /**
     * Rating for this service (after completion)
     * One-to-one: 1 request → 1 rating
     */
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private Rating rating;
}