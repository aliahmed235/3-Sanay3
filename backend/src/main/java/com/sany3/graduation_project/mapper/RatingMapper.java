package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.dto.response.RatingResponse;
import com.sany3.graduation_project.entites.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

    public RatingResponse toRatingResponse(Rating rating) {
        if (rating == null) return null;

        String reputationLabel = getReputationLabel(rating.getFinalRating());

        return RatingResponse.builder()
                .id(rating.getId())
                .requestId(rating.getRequest().getId())           // ✅ ADD
                .providerId(rating.getProvider().getId())          // ✅ ADD
                .providerName(rating.getProvider().getName())      // ✅ ADD
                .customerId(rating.getCustomer().getId())          // ✅ ADD
                .ratingValue(rating.getRatingValue())
                .review(rating.getReview())
                .totalPenalty(rating.getTotalPenalty() != null ? rating.getTotalPenalty() : 0.0) // ✅ NULL CHECK
                .finalRating(rating.getFinalRating())
                .reputationLabel(reputationLabel)
                .cancellationPenalty(rating.getCancellationPenalty() != null ? rating.getCancellationPenalty() : 0.0) // ✅ NULL CHECK
                .lateArrivalPenalty(rating.getLateArrivalPenalty() != null ? rating.getLateArrivalPenalty() : 0.0) // ✅ NULL CHECK
                .minutesLate(rating.getMinutesLate())
                .incompleteServicePenalty(rating.getIncompleteServicePenalty() != null ? rating.getIncompleteServicePenalty() : 0.0) // ✅ NULL CHECK
                .incompleteServiceReason(rating.getIncompleteServiceReason())
                .createdAt(rating.getCreatedAt())
                .build();
    }

    private String getReputationLabel(Double rating) {
        if (rating == null || rating == 0) return "No rating yet";
        if (rating >= 4.5) return "⭐⭐⭐⭐⭐ Excellent";           // ✅ ADD EMOJIS
        if (rating >= 4.0) return "⭐⭐⭐⭐ Very Good";
        if (rating >= 3.5) return "⭐⭐⭐ Good";
        if (rating >= 3.0) return "⭐⭐ Average";
        return "⭐ Needs Improvement";
    }
}