package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.dto.response.RatingResponse;
import com.sany3.graduation_project.dto.response.ServiceRequestResponse;
import com.sany3.graduation_project.entites.PhotoType;
import com.sany3.graduation_project.entites.ServiceRequest;
import com.sany3.graduation_project.entites.WorkPhoto;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ServiceRequestMapper {

    private final UserMapper userMapper;
    private final RatingMapper ratingMapper;

    public ServiceRequestResponse toServiceRequestResponse(ServiceRequest request) {
        if (request == null) return null;

        RatingResponse ratingResponse = null;
        if (Hibernate.isInitialized(request.getRating()) && request.getRating() != null) {
            ratingResponse = ratingMapper.toRatingResponse(request.getRating());
        }

        Long offerCount = null;
        if (Hibernate.isInitialized(request.getOffers()) && request.getOffers() != null) {
            offerCount = (long) request.getOffers().size();
        }

        List<String> beforePhotos = Collections.emptyList();
        List<String> afterPhotos = Collections.emptyList();
        if (Hibernate.isInitialized(request.getWorkPhotos()) && request.getWorkPhotos() != null) {
            beforePhotos = request.getWorkPhotos().stream()
                    .filter(p -> p.getPhotoType() == PhotoType.BEFORE)
                    .map(WorkPhoto::getPhotoUrl)
                    .toList();
            afterPhotos = request.getWorkPhotos().stream()
                    .filter(p -> p.getPhotoType() == PhotoType.AFTER)
                    .map(WorkPhoto::getPhotoUrl)
                    .toList();
        }

        return ServiceRequestResponse.builder()
                .id(request.getId())
                .serviceType(request.getServiceType())
                .title(request.getTitle())
                .description(request.getDescription())
                .photoUrl(request.getPhotoUrl())
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
                .scheduledAt(request.getScheduledAt())
                .offerCount(offerCount)
                .rating(ratingResponse)
                .workSummary(request.getWorkSummary())
                .beforePhotos(beforePhotos)
                .afterPhotos(afterPhotos)
                .build();
    }
}
