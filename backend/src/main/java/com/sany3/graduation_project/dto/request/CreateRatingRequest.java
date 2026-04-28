package com.sany3.graduation_project.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRatingRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be 1-5 stars")
    @Max(value = 5, message = "Rating must be 1-5 stars")
    private Integer ratingValue;

    @Size(min = 10, max = 500, message = "Review must be 10-500 characters")
    private String review;

    @Min(value = 0, message = "Minutes late cannot be negative")
    private Integer minutesLate;

    private Double lateArrivalPenalty;
    private Double incompleteServicePenalty;
    private String incompleteServiceReason;
}