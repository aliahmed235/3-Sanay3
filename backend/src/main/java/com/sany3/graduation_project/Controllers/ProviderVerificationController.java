package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.ProviderVerificationService;
import com.sany3.graduation_project.dto.request.ReapplyProviderRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.ProviderVerificationStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Provider-facing verification endpoints.
 * Lets a provider see their own verification status in-app and re-apply after
 * a rejection.
 */
@RestController
@RequestMapping("/api/provider/verification")
@RequiredArgsConstructor
@Slf4j
public class ProviderVerificationController {

    private final ProviderVerificationService providerVerificationService;

    /**
     * GET /api/provider/verification-status
     * Current verification state of the logged-in provider.
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<ProviderVerificationStatusResponse>> getMyStatus(
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ProviderVerificationStatusResponse response = providerVerificationService.getMyStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Verification status retrieved"));
    }

    /**
     * PUT /api/provider/verification/reapply (multipart/form-data)
     * A rejected provider edits their info and uploads new photos, then resubmits.
     * Send text fields as form fields and optionally attach `profilePicture` and
     * `criminalHistory` files.
     */
    @PutMapping(value = "/reapply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProviderVerificationStatusResponse>> reapplyMultipart(
            @Valid @ModelAttribute ReapplyProviderRequest request,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "criminalHistory", required = false) MultipartFile criminalHistory,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ProviderVerificationStatusResponse response = providerVerificationService
                .reapply(userId, request, profilePicture, criminalHistory);
        return ResponseEntity.ok(ApiResponse.success(response,
                "Application resubmitted. It is now pending admin review."));
    }

    /**
     * PUT /api/provider/verification/reapply (application/json)
     * Same as above but with image URLs instead of file uploads.
     */
    @PutMapping(value = "/reapply", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProviderVerificationStatusResponse>> reapplyJson(
            @Valid @RequestBody ReapplyProviderRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ProviderVerificationStatusResponse response = providerVerificationService.reapply(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response,
                "Application resubmitted. It is now pending admin review."));
    }
}
