package com.sany3.graduation_project.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sany3.graduation_project.util.Constants;

/**
 * DTO for customer registration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

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
     *
     * Examples:
     * ✅ MyPass123
     * ✅ SecurePass456
     * ❌ password123 (no uppercase)
     * ❌ MyPassword (no number)
     * ❌ pass123 (too short)
     */
    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = Constants.VALIDATION.PASSWORD_PATTERN,
            message = "Password must be at least 8 characters, " +
                    "contain at least 1 uppercase letter and 1 number"
    )
    private String password;
}