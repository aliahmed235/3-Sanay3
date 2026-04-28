// ProviderRatingStats.java
package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderRatingStats {
    private Long providerId;
    private Double averageRating;
    private Long totalRatings;
    private Double totalEarnings;

    /**
     * Get provider's reputation label
     */
    public String getReputationLabel() {
        if (averageRating == null || averageRating == 0) {
            return "No ratings yet";
        }
        if (averageRating >= 4.5) return "Excellent";
        if (averageRating >= 4.0) return "Very Good";
        if (averageRating >= 3.5) return "Good";
        if (averageRating >= 3.0) return "Average";
        return "Needs Improvement";
    }
}