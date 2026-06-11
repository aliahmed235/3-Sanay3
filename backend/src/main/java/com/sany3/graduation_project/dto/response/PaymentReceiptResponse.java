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
public class PaymentReceiptResponse {

    private String referenceNumber;
    private String type;
    private BigDecimal amount;
    private String providerName;
    private String providerPhone;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
