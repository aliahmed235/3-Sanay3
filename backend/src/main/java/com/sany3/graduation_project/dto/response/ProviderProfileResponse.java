package com.sany3.graduation_project.dto.response;

import com.sany3.graduation_project.entites.ServiceType;
import com.sany3.graduation_project.entites.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Full provider profile
 * Shows on provider detail page
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderProfileResponse {

    private Long providerId;
    private String name;
    private String email;
    private String phone;
    private String profileImage;
    private String bio;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private ServiceType serviceType;
    private BigDecimal hourlyRate;
    private Boolean isVerified;
    private VerificationStatus verificationStatus;
    private Double averageRating;
    private Long totalRatings;
    private Integer completionRate;
    private String reputationLabel;
    private Long totalJobs;
}