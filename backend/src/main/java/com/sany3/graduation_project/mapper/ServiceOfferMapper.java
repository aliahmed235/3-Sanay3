package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.Repositories.RatingRepository;
import com.sany3.graduation_project.dto.response.ProviderPreviewResponse;
import com.sany3.graduation_project.dto.response.ServiceOfferResponse;
import com.sany3.graduation_project.entites.ServiceOffer;
import com.sany3.graduation_project.entites.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceOfferMapper {

    private final RatingRepository ratingRepository;

    public ServiceOfferResponse toServiceOfferResponse(ServiceOffer offer) {
        if (offer == null) return null;

        ProviderPreviewResponse provider = buildProviderPreview(offer.getProvider());

        return ServiceOfferResponse.builder()
                .id(offer.getId())
                .requestId(offer.getRequest().getId())
                .provider(provider)
                .offeredPrice(offer.getOfferedPrice())
                .estimatedTimeMinutes(offer.getEstimatedTimeMinutes())
                .description(offer.getDescription())
                .status(offer.getStatus())
                .createdAt(offer.getCreatedAt())
                .respondedAt(offer.getRespondedAt())
                .build();
    }

    private ProviderPreviewResponse buildProviderPreview(User provider) {
        if (provider == null) return null;

        Double avgRating = ratingRepository.calculateAverageRating(provider.getId());
        Long totalRatings = ratingRepository.countByProviderId(provider.getId());

        return ProviderPreviewResponse.builder()
                .id(provider.getId())
                .name(provider.getName())
                .profileImage(provider.getProfileImage())
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalRatings(totalRatings != null ? totalRatings : 0L)
                .latitude(provider.getLatitude())
                .longitude(provider.getLongitude())
                .build();
    }
}