package com.sany3.graduation_project.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Work Photo Entity
 * Photos uploaded by provider after completing a job
 * Shows how they fixed the problem (portfolio)
 */
@Entity
@Table(name = "work_photos", indexes = {
        @Index(name = "idx_work_photo_request", columnList = "request_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ServiceRequest serviceRequest;

    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "photo_type", nullable = false)
    @Builder.Default
    private PhotoType photoType = PhotoType.AFTER;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
