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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String photoUrl;

    @Column(length = 255)
    private String address;

    @Column(columnDefinition = "DECIMAL(10, 8)", nullable = false)
    private BigDecimal latitude;

    @Column(columnDefinition = "DECIMAL(10, 8)", nullable = false)
    private BigDecimal longitude;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_provider_id")
    private User acceptedProvider;

    @Column
    private LocalDateTime acceptedAt;

    @Column
    private LocalDateTime startedAt;


    @Column
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime scheduledAt;


    @Column(columnDefinition = "TEXT")
    private String workSummary;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOffer> offers;


    @OneToMany(mappedBy = "serviceRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkPhoto> workPhotos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private Rating rating;
}
