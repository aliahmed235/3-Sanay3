package com.sany3.graduation_project.dto.response;

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
public class PaymentResponse {

    private Long id;
    private Long serviceRequestId;
    private String serviceRequestTitle;
    private Long customerId;
    private String customerName;
    private Long providerId;
    private String providerName;
    private BigDecimal amount;
    private String paymentMethod;
    private BigDecimal platformFee;
    private BigDecimal providerEarning;
    private String status;
    private LocalDateTime createdAt;
}
