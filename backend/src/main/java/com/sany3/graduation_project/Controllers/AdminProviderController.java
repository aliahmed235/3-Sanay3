package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.AdminProviderVerificationService;
import com.sany3.graduation_project.dto.request.RejectProviderRequest;
import com.sany3.graduation_project.dto.response.AdminProviderResponse;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.VerificationStatsResponse;
import com.sany3.graduation_project.mapper.AdminProviderMapper;
import com.sany3.graduation_project.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/providers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminProviderController {

    private final AdminProviderVerificationService verificationService;
    private final AdminProviderMapper adminProviderMapper;

    /**
     * GET /admin/providers/pending - List pending providers (paginated)
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<AdminProviderResponse>>> getPendingProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AdminProviderResponse> response = verificationService.getPendingProviders(pageable)
                .map(adminProviderMapper::toAdminProviderResponse);

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.RESOURCE_RETRIEVED));
    }

    /**
     * GET /admin/providers/{providerId} - Provider details with documents
     */
    @GetMapping("/{profileId}")
    public ResponseEntity<ApiResponse<AdminProviderResponse>> getProviderDetails(
            @PathVariable Long profileId) {

        AdminProviderResponse response = adminProviderMapper
                .toAdminProviderResponse(verificationService.getProviderDetails(profileId));

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.RESOURCE_RETRIEVED));
    }

    /**
     * PUT /admin/providers/{providerId}/approve - Approve a provider
     */
    @PutMapping("/{profileId}/approve")
    public ResponseEntity<ApiResponse<AdminProviderResponse>> approveProvider(
            @PathVariable Long profileId,
            Authentication authentication) {

        Long adminUserId = (Long) authentication.getPrincipal();

        AdminProviderResponse response = adminProviderMapper
                .toAdminProviderResponse(verificationService.approveProvider(profileId, adminUserId));

        return ResponseEntity.ok(ApiResponse.success(response, "Provider approved successfully"));
    }

    /**
     * PUT /admin/providers/{providerId}/reject - Reject a provider
     */
    @PutMapping("/{profileId}/reject")
    public ResponseEntity<ApiResponse<AdminProviderResponse>> rejectProvider(
            @PathVariable Long profileId,
            @Valid @RequestBody RejectProviderRequest request,
            Authentication authentication) {

        Long adminUserId = (Long) authentication.getPrincipal();

        AdminProviderResponse response = adminProviderMapper
                .toAdminProviderResponse(verificationService.rejectProvider(profileId, adminUserId, request.getRejectionReason()));

        return ResponseEntity.ok(ApiResponse.success(response, "Provider rejected"));
    }

    /**
     * GET /admin/providers/stats - Verification statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<VerificationStatsResponse>> getVerificationStats() {

        VerificationStatsResponse response = verificationService.getVerificationStats();

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.RESOURCE_RETRIEVED));
    }
}
