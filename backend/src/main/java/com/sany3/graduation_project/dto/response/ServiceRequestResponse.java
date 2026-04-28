package com.sany3.graduation_project.dto.response;

import com.sany3.graduation_project.entites.RequestStatus;
import com.sany3.graduation_project.entites.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service request response DTO
 * What customer sees on their request
 * Budget removed - providers set price in their offers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestResponse {

    private Long id;
    private ServiceType serviceType;
    private String title;
    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private RequestStatus status;
    private UserResponse customer;
    private UserResponse acceptedProvider;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long offerCount;  // How many providers offered
    private RatingResponse rating;
}