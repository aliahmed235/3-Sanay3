package com.sany3.graduation_project.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateServiceOfferRequest {

    /**
     * Request ID this offer is for
     */
    @NotNull(message = "Request ID is required")
    private Long requestId;

    /**
     * Price offered for the service
     */
    @NotNull(message = "Offered price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal offeredPrice;

    /**
     * Estimated time to complete (in minutes)
     */
    @NotNull(message = "Estimated time is required")
    @Positive(message = "Time must be positive")
    private Integer estimatedTimeMinutes;

    /**
     * Description/notes about the offer
     */
    private String description;
}