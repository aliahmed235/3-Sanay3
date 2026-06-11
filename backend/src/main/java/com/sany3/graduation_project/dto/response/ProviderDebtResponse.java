package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderDebtResponse {

    private Long providerId;
    private String providerName;
    private String providerEmail;
    private String providerPhone;
    private BigDecimal balance;
    private Boolean banned;
}
