package com.sany3.graduation_project.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestExtensionRequest {

    @NotNull(message = "Additional days is required")
    @Min(value = 1, message = "Additional days must be at least 1")
    @Max(value = 30, message = "Additional days must not exceed 30")
    private Integer additionalDays;

    @NotNull(message = "Updated price is required")
    @DecimalMin(value = "0.01", message = "Updated price must be at least 0.01")
    @DecimalMax(value = "1000000.00", message = "Updated price must not exceed 1,000,000")
    private BigDecimal updatedPrice;

    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;
}
