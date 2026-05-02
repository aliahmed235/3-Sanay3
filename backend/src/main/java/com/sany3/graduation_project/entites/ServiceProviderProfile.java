package com.sany3.graduation_project.entites;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "service_provider_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "providerDocuments", "verifiedByAdmin"})
@EqualsAndHashCode(exclude = {"user", "providerDocuments", "verifiedByAdmin"})
public class ServiceProviderProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_service_provider_profiles_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ServiceType serviceType; // CARPENTER, WATER, ELECTRICITY

    @Column(name = "national_id", length = 20, unique = true)
    private String nationalId;

    @Column(precision = 8, scale = 2)
    private BigDecimal hourlyRate;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 255)
    private String address;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 8)
    private BigDecimal longitude;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_admin_id",
            foreignKey = @ForeignKey(name = "fk_service_provider_verified_by_admin"))
    private User verifiedByAdmin;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "has_criminal_record", nullable = false)
    private Boolean hasCriminalRecord = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "serviceProviderProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProviderDocument> providerDocuments = new HashSet<>();

    // Helper methods
    public boolean isPending() {
        return verificationStatus == VerificationStatus.PENDING;
    }

    public boolean isApproved() {
        return verificationStatus == VerificationStatus.APPROVED && isVerified;
    }

    public boolean isRejected() {
        return verificationStatus == VerificationStatus.REJECTED;
    }
}
