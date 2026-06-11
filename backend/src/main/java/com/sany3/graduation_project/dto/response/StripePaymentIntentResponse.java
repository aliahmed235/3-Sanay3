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
public class StripePaymentIntentResponse {

    private String clientSecret;
    private String paymentIntentId;
    private BigDecimal amount;
    private String currency;
}
