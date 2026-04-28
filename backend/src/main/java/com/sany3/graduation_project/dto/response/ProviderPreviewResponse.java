package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Compact provider info shown in offer lists
 * Shows rating, distance, etc. (not full profile)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderPreviewResponse {

    private Long id;
    private String name;
    private String profileImage;
    private Double averageRating;
    private Long totalRatings;
    private Integer acceptanceRate;
    private Integer completionRate;
    private BigDecimal latitude;
    private BigDecimal longitude;
}