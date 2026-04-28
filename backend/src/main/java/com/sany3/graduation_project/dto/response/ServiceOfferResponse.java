package com.sany3.graduation_project.dto.response;

import com.sany3.graduation_project.entites.OfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service offer response DTO
 * What customer sees when choosing offers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOfferResponse {

    private Long id;
    private Long requestId;
    private ProviderPreviewResponse provider;
    private BigDecimal offeredPrice;
    private Integer estimatedTimeMinutes;
    private String description;
    private OfferStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}