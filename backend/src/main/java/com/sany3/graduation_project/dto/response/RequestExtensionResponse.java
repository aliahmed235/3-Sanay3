package com.sany3.graduation_project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sany3.graduation_project.entites.ExtensionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestExtensionResponse {

    private Long id;
    private Long serviceRequestId;
    private Long providerId;
    private String providerName;
    private Integer additionalDays;
    private BigDecimal originalPrice;
    private BigDecimal updatedPrice;
    private String reason;
    private ExtensionStatus status;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
}
