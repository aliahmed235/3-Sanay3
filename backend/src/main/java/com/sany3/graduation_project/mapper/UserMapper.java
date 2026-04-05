package com.sany3.graduation_project.mapper;

import com.sany3.graduation_project.dto.response.UserResponse;
import com.sany3.graduation_project.entites.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getUserRoles().stream()
                        .map(ur -> ur.getRole().getName().toString())
                        .collect(Collectors.toSet()))
                .isActive(user.getIsActive())
                .profileImage(user.getProfileImage())
                .build();
    }
}