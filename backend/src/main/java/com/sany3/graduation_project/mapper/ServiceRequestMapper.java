package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.dto.response.RatingResponse;
import com.sany3.graduation_project.dto.response.ServiceRequestResponse;
import com.sany3.graduation_project.entites.ServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceRequestMapper {

    private final UserMapper userMapper;
    private final RatingMapper ratingMapper;

    public ServiceRequestResponse toServiceRequestResponse(ServiceRequest request) {
        if (request == null) return null;

        RatingResponse ratingResponse = null;
        if (request.getRating() != null) {
            ratingResponse = ratingMapper.toRatingResponse(request.getRating());
        }

        return ServiceRequestResponse.builder()
                .id(request.getId())
                .serviceType(request.getServiceType())
                .title(request.getTitle())
                .description(request.getDescription())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(request.getStatus())
                .customer(userMapper.toUserResponse(request.getCustomer()))
                .acceptedProvider(userMapper.toUserResponse(request.getAcceptedProvider()))
                .acceptedAt(request.getAcceptedAt())
                .startedAt(request.getStartedAt())
                .completedAt(request.getCompletedAt())
                .createdAt(request.getCreatedAt())
                .expiresAt(request.getExpiresAt())
                .offerCount((long) (request.getOffers() != null ? request.getOffers().size() : 0))
                .rating(ratingResponse)
                .build();
    }
}