package com.sany3.graduation_project.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sany3.graduation_project.util.Constants;

/**
 * DTO for submitting a rating
 *
 * Used after service completion
 * Customer submits rating with penalties info
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingCreateRequest {

    /**
     * Service request ID being rated
     */
    @NotNull(message = "Request ID is required")
    private Long requestId;

    /**
     * Star rating from customer (1-5)
     */
    @NotNull(message = "Rating is required")
    @Min(value = Constants.RATING.MIN_RATING,
            message = "Rating must be at least 1")
    @Max(value = Constants.RATING.MAX_RATING,
            message = "Rating must be at most 5")
    private Integer ratingValue;

    /**
     * Optional written review
     */
    @Size(max = Constants.RATING.MAX_REVIEW_LENGTH,
            message = "Review must not exceed 500 characters")
    private String review;

    /**
     * Did provider cancel after accepting?
     * Default: false (no cancellation)
     */
    @NotNull(message = "Cancellation flag is required")
    private Boolean hadCancellation;

    /**
     * How many minutes late was provider?
     * Null if on time
     *
     * Only set if late
     * Example: 15 (provider was 15 minutes late)
     */
    @Min(value = 0, message = "Minutes late cannot be negative")
    private Integer minutesLate;

    /**
     * Was service incomplete/not fixed?
     * Default: false (service was complete)
     */
    @NotNull(message = "Service completion flag is required")
    private Boolean isIncompleteService;

    /**
     * Reason for incomplete service
     * Required if isIncompleteService = true
     *
     * Example: "Gas is still not working"
     */
    @Size(max = Constants.RATING.MAX_REVIEW_LENGTH,
            message = "Reason must not exceed 500 characters")
    private String incompleteReason;
}