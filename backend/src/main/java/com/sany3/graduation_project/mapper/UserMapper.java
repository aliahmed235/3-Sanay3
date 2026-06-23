package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.dto.response.UserResponse;
import com.sany3.graduation_project.entites.ServiceType;
import com.sany3.graduation_project.entites.User;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * Convert User entity to response DTO
     * Excludes sensitive data like password
     */
    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        String role = null;
        if (Hibernate.isInitialized(user.getUserRoles()) && user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
            role = user.getRoleNames().stream().findFirst().orElse(null);
        }

        ServiceType serviceType = null;
        String verificationStatus = null;
        Boolean isVerified = null;
        String rejectionReason = null;
        if (Hibernate.isInitialized(user.getServiceProviderProfile()) && user.getServiceProviderProfile() != null) {
            var profile = user.getServiceProviderProfile();
            serviceType = profile.getServiceType();
            isVerified = profile.getIsVerified();
            verificationStatus = profile.getVerificationStatus() != null
                    ? profile.getVerificationStatus().name() : null;
            rejectionReason = profile.getRejectionReason();
        }

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .address(user.getAddress())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .isActive(user.getIsActive())
                .role(role)
                .serviceType(serviceType)
                .verificationStatus(verificationStatus)
                .isVerified(isVerified)
                .rejectionReason(rejectionReason)
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Convert response DTO to entity
     */
    public User toEntity(UserResponse response) {
        if (response == null) {
            return null;
        }

        User user = new User();
        user.setId(response.getId());
        user.setName(response.getName());
        user.setEmail(response.getEmail());
        user.setPhone(response.getPhone());
        user.setAddress(response.getAddress());
        user.setLatitude(response.getLatitude());
        user.setLongitude(response.getLongitude());
        user.setProfileImage(response.getProfileImage());
        user.setIsActive(response.getIsActive());
        return user;
    }
}