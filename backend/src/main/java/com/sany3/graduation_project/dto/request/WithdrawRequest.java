package com.sany3.graduation_project.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    /**
     * CASH (collect from supermarket) or CREDIT_CARD (2-step Stripe flow, test mode).
     * Optional — defaults to CASH when omitted for backward compatibility.
     */
    private String paymentMethod;
}
