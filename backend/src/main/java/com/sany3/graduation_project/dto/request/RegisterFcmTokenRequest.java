package com.sany3.graduation_project.dto.request;

import com.sany3.graduation_project.entites.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterFcmTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String token;

    @NotNull(message = "Platform is required (ANDROID or IOS)")
    private DevicePlatform platform;
}
