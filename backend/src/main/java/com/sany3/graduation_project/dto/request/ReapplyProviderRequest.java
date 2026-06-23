package com.sany3.graduation_project.dto.request;

import com.sany3.graduation_project.entites.ServiceType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A rejected provider edits their info/documents and resubmits for review.
 * All fields optional — only provided fields are updated; the rest keep their
 * current values. Account credentials (name/email/phone/password) are not here.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReapplyProviderRequest {

    private ServiceType serviceType;

    @Pattern(regexp = "^[0-9]{14}$", message = "National ID must be exactly 14 digits")
    private String nationalId;

    @DecimalMin(value = "0.01", message = "Hourly rate must be at least 0.01")
    @DecimalMax(value = "10000.00", message = "Hourly rate cannot exceed 10000")
    private BigDecimal hourlyRate;

    @Size(min = 10, max = 500, message = "Bio must be between 10 and 500 characters")
    private String bio;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    private Boolean hasCriminalRecord;

    private String profileImageUrl;

    private String criminalHistoryDocumentUrl;
}
