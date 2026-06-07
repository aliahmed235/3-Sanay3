package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.ProfileService;
import com.sany3.graduation_project.dto.request.ChangePasswordRequest;
import com.sany3.graduation_project.dto.request.UpdateAddressRequest;
import com.sany3.graduation_project.dto.request.UpdateHourlyRateRequest;
import com.sany3.graduation_project.dto.request.UpdateNameRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Get current user's profile
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Get profile for user {}", userId);
        UserResponse profile = profileService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    /**
     * Update name
     */
    @PutMapping("/name")
    public ResponseEntity<ApiResponse<UserResponse>> updateName(
            Authentication authentication,
            @Valid @RequestBody UpdateNameRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("User {} updating name", userId);
        UserResponse updated = profileService.updateName(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Name updated successfully"));
    }

    /**
     * Change password
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("User {} changing password", userId);
        profileService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    /**
     * Upload or change profile photo
     */
    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updatePhoto(
            Authentication authentication,
            @RequestPart("photo") MultipartFile photo) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("User {} updating photo", userId);
        UserResponse updated = profileService.updatePhoto(userId, photo);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile photo updated successfully"));
    }

    /**
     * Remove profile photo
     */
    @DeleteMapping("/photo")
    public ResponseEntity<ApiResponse<UserResponse>> removePhoto(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("User {} removing photo", userId);
        UserResponse updated = profileService.removePhoto(userId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile photo removed successfully"));
    }

    /**
     * Update address (works for both customer and provider)
     */
    @PutMapping("/address")
    public ResponseEntity<ApiResponse<UserResponse>> updateAddress(
            Authentication authentication,
            @Valid @RequestBody UpdateAddressRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("User {} updating address", userId);
        UserResponse updated = profileService.updateAddress(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Address updated successfully"));
    }

    /**
     * Update hourly rate (provider only)
     */
    @PutMapping("/hourly-rate")
    public ResponseEntity<ApiResponse<UserResponse>> updateHourlyRate(
            Authentication authentication,
            @Valid @RequestBody UpdateHourlyRateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("User {} updating hourly rate", userId);
        UserResponse updated = profileService.updateHourlyRate(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Hourly rate updated successfully"));
    }
}
