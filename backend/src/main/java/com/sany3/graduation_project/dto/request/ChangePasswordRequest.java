package com.sany3.graduation_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$",
            message = "Password must be at least 8 characters with at least one uppercase letter and one number"
    )
    private String newPassword;
}
