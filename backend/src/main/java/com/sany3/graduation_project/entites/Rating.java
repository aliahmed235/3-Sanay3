package com.sany3.graduation_project.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", unique = true, nullable = false)
    private ServiceRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Column(nullable = false)
    private Integer ratingValue;

    @Column(columnDefinition = "TEXT", length = 500)
    private String review;

    // CHANGE THESE FROM Boolean TO Double
    @Column(nullable = false)
    private Double cancellationPenalty;

    @Column(nullable = false)
    private Double lateArrivalPenalty;

    @Column
    private Integer minutesLate;

    @Column(nullable = false)
    private Double incompleteServicePenalty;

    @Column(columnDefinition = "TEXT", length = 500)
    private String incompleteServiceReason;

    @Column
    private Double totalPenalty;

    @Column
    private Double finalRating;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}