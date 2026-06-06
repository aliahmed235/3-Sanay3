package com.sany3.graduation_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProviderResponse {

    // User info
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String profileImage;

    // Provider profile info
    private Long profileId;
    private String serviceType;
    private String nationalId;
    private BigDecimal hourlyRate;
    private String bio;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean hasCriminalRecord;

    // Verification info
    private Boolean isVerified;
    private String verificationStatus;
    private String verifiedByAdminName;
    private LocalDateTime verificationDate;
    private String rejectionReason;

    // Documents
    private List<ProviderDocumentResponse> documents;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
