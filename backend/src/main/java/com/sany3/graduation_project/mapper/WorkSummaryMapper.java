package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.dto.response.WorkSummaryResponse;
import com.sany3.graduation_project.entites.ServiceRequest;
import com.sany3.graduation_project.entites.WorkPhoto;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class WorkSummaryMapper {

    public WorkSummaryResponse toWorkSummaryResponse(ServiceRequest request) {
        if (request == null) return null;

        List<String> photoUrls = Collections.emptyList();
        if (Hibernate.isInitialized(request.getWorkPhotos()) && request.getWorkPhotos() != null) {
            photoUrls = request.getWorkPhotos().stream()
                    .map(WorkPhoto::getPhotoUrl)
                    .toList();
        }

        return WorkSummaryResponse.builder()
                .requestId(request.getId())
                .requestTitle(request.getTitle())
                .description(request.getWorkSummary())
                .photos(photoUrls)
                .customerName(request.getCustomer() != null ? request.getCustomer().getName() : null)
                .serviceType(request.getServiceType() != null ? request.getServiceType().toString() : null)
                .completedAt(request.getCompletedAt())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
