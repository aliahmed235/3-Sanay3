package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.dto.response.WorkSummaryResponse;
import com.sany3.graduation_project.entites.PhotoType;
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

        return WorkSummaryResponse.builder()
                .requestId(request.getId())
                .requestTitle(request.getTitle())
                .description(request.getWorkSummary())
                .beforePhotos(beforePhotos)
                .afterPhotos(afterPhotos)
                .customerName(request.getCustomer() != null ? request.getCustomer().getName() : null)
                .serviceType(request.getServiceType() != null ? request.getServiceType().toString() : null)
                .completedAt(request.getCompletedAt())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
