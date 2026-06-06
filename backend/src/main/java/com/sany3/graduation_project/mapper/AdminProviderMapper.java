package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.dto.response.AdminProviderResponse;
import com.sany3.graduation_project.dto.response.ProviderDocumentResponse;
import com.sany3.graduation_project.entites.ProviderDocument;
import com.sany3.graduation_project.entites.ServiceProviderProfile;
import com.sany3.graduation_project.entites.User;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AdminProviderMapper {

    public AdminProviderResponse toAdminProviderResponse(ServiceProviderProfile profile) {
        if (profile == null) {
            return null;
        }

        User user = profile.getUser();

        String verifiedByName = null;
        if (profile.getVerifiedByAdmin() != null && Hibernate.isInitialized(profile.getVerifiedByAdmin())) {
            verifiedByName = profile.getVerifiedByAdmin().getName();
        }

        List<ProviderDocumentResponse> documents = Collections.emptyList();
        if (Hibernate.isInitialized(profile.getProviderDocuments()) && profile.getProviderDocuments() != null) {
            documents = profile.getProviderDocuments().stream()
                    .map(this::toProviderDocumentResponse)
                    .toList();
        }

        return AdminProviderResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .profileId(profile.getId())
                .serviceType(profile.getServiceType().name())
                .nationalId(profile.getNationalId())
                .hourlyRate(profile.getHourlyRate())
                .bio(profile.getBio())
                .address(profile.getAddress())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .hasCriminalRecord(profile.getHasCriminalRecord())
                .isVerified(profile.getIsVerified())
                .verificationStatus(profile.getVerificationStatus().name())
                .verifiedByAdminName(verifiedByName)
                .verificationDate(profile.getVerificationDate())
                .rejectionReason(profile.getRejectionReason())
                .documents(documents)
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    public ProviderDocumentResponse toProviderDocumentResponse(ProviderDocument document) {
        if (document == null) {
            return null;
        }

        return ProviderDocumentResponse.builder()
                .id(document.getId())
                .documentType(document.getDocumentType().name())
                .documentName(document.getDocumentName())
                .documentUrl(document.getDocumentUrl())
                .isVerified(document.getIsVerified())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}
