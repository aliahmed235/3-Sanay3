package com.sany3.graduation_project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * What a provider sees in-app about their own verification state.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProviderVerificationStatusResponse {

    private String verificationStatus; // PENDING | APPROVED | REJECTED
    private Boolean isVerified;
    private String rejectionReason;    // only when REJECTED
    private LocalDateTime verificationDate;
    private Boolean canReapply;        // true only when REJECTED
}
