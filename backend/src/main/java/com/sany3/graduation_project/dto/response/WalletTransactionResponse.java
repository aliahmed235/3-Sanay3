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
public class WalletTransactionResponse {

    private Long id;
    private String type;
    private String typeDisplayName;
    private BigDecimal amount;
    private String description;
    private Long serviceRequestId;
    private String serviceRequestTitle;
    private LocalDateTime createdAt;
}
