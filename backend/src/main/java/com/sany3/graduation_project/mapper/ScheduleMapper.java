package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.Repositories.ServiceOfferRepository;
import com.sany3.graduation_project.dto.response.ScheduleDayResponse;
import com.sany3.graduation_project.entites.OfferStatus;
import com.sany3.graduation_project.entites.ServiceOffer;
import com.sany3.graduation_project.entites.ServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleMapper {

    private final ServiceOfferRepository serviceOfferRepository;

    public ScheduleDayResponse toScheduleDayResponse(ServiceRequest request) {
        if (request == null) return null;

        Integer estimatedTime = null;
        if (request.getAcceptedProvider() != null) {
            estimatedTime = serviceOfferRepository
                    .findByRequestIdAndStatusOrderByCreatedAtAsc(request.getId(), OfferStatus.ACCEPTED)
                    .map(ServiceOffer::getEstimatedTimeMinutes)
                    .orElse(null);
        }

        return ScheduleDayResponse.builder()
                .requestId(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .address(request.getAddress())
                .serviceType(request.getServiceType())
                .status(request.getStatus())
                .scheduledAt(request.getScheduledAt())
                .acceptedAt(request.getAcceptedAt())
                .customerName(request.getCustomer() != null ? request.getCustomer().getName() : null)
                .customerPhone(request.getCustomer() != null ? request.getCustomer().getPhone() : null)
                .customerAvatar(request.getCustomer() != null ? request.getCustomer().getProfileImage() : null)
                .estimatedTimeMinutes(estimatedTime)
                .build();
    }
}
