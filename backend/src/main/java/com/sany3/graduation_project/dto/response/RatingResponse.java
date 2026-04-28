package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Rating response DTO
 * Shows rating details with penalties
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingResponse {
    private Long id;
    private Long requestId;        // ✅ ADD
    private Long providerId;       // ✅ ADD
    private String providerName;   // ✅ ADD
    private Long customerId;       // ✅ ADD
    private Integer ratingValue;
    private String review;
    private Double totalPenalty;
    private Double finalRating;
    private String reputationLabel;
    private Double cancellationPenalty;
    private Double lateArrivalPenalty;
    private Integer minutesLate;
    private Double incompleteServicePenalty;
    private String incompleteServiceReason;
    private LocalDateTime createdAt;
}