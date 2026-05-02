package com.sany3.graduation_project.dto.request;

import com.sany3.graduation_project.entites.ServiceType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sany3.graduation_project.util.Constants;
import java.math.BigDecimal;

/**
 * DTO for service provider registration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterProviderRequest {

    @NotBlank(message = "Name is required")
    @Size(min = Constants.VALIDATION.MIN_NAME_LENGTH,
            max = Constants.VALIDATION.MAX_NAME_LENGTH,
            message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = Constants.VALIDATION.MAX_EMAIL_LENGTH,
            message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = Constants.VALIDATION.PHONE_PATTERN,
            message = "Phone must be at least 10 digits")
    private String phone;

    /**
     * Password requirements:
     * - 8+ characters
     * - At least 1 UPPERCASE letter
     * - At least 1 number
     */
    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = Constants.VALIDATION.PASSWORD_PATTERN,
            message = "Password must be at least 8 characters, " +
                    "contain at least 1 uppercase letter and 1 number"
    )
    private String password;

    @NotNull(message = "Service type is required")
    ServiceType serviceType;

    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "^[0-9]{14}$", message = "National ID must be exactly 14 digits")
    private String nationalId;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.01", message = "Hourly rate must be at least 0.01")
    @DecimalMax(value = "10000.00", message = "Hourly rate cannot exceed 10000")
    private BigDecimal hourlyRate;

    @NotBlank(message = "Bio is required")
    @Size(min = Constants.VALIDATION.MIN_BIO_LENGTH,
            max = Constants.VALIDATION.MAX_BIO_LENGTH,
            message = "Bio must be between 10 and 500 characters")
    private String bio;

    @NotBlank(message = "Address is required")
    @Size(max = Constants.VALIDATION.MAX_ADDRESS_LENGTH,
            message = "Address must not exceed 255 characters")
    private String address;

    @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    @Builder.Default
    private Boolean hasCriminalRecord = false;

    private String profileImageUrl;

    private String criminalHistoryDocumentUrl;
}
