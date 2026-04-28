package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.dto.response.UserResponse;
import com.sany3.graduation_project.entites.User;
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