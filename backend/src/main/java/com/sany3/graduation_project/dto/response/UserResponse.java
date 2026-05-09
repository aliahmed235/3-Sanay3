package com.sany3.graduation_project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.sany3.graduation_project.entites.ServiceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String profileImage;
    private Boolean isActive;
    private String role;           // "USER" or "SERVICE_PROVIDER"
    private ServiceType serviceType; // only for providers (CARPENTER, WATER, ELECTRICITY)
    private String profilePictureUrl;
    private LocalDateTime createdAt;
}