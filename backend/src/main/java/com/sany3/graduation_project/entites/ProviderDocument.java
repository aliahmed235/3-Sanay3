package com.sany3.graduation_project.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "provider_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "serviceProviderProfile")
@EqualsAndHashCode(exclude = "serviceProviderProfile")
public class ProviderDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_provider_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_provider_documents_service_provider"))
    private ServiceProviderProfile serviceProviderProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentType documentType; // ID, LICENSE, CERTIFICATE, INSURANCE, OTHER

    @Column(nullable = false, length = 255)
    private String documentName;

    @Column(nullable = false, length = 500)
    private String documentUrl;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}