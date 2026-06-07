package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.RequestExtensionService;
import com.sany3.graduation_project.dto.request.RequestExtensionRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.RequestExtensionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@Validated
@RequiredArgsConstructor
@Slf4j
public class RequestExtensionController {

    private final RequestExtensionService extensionService;

    /**
     * Provider requests extension (more days + updated price)
     */
    @PostMapping("/{requestId}/extend")
    public ResponseEntity<ApiResponse<RequestExtensionResponse>> requestExtension(
            Authentication authentication,
            @PathVariable Long requestId,
            @Valid @RequestBody RequestExtensionRequest request) {
        Long providerId = (Long) authentication.getPrincipal();
        log.info("Provider {} requesting extension for request {}", providerId, requestId);
        RequestExtensionResponse response = extensionService.requestExtension(
                providerId, requestId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Extension requested successfully"));
    }

    /**
     * Customer approves extension
     */
    @PutMapping("/{requestId}/extend/approve")
    public ResponseEntity<ApiResponse<RequestExtensionResponse>> approveExtension(
            Authentication authentication,
            @PathVariable Long requestId) {
        Long customerId = (Long) authentication.getPrincipal();
        log.info("Customer {} approving extension for request {}", customerId, requestId);
        RequestExtensionResponse response = extensionService.approveExtension(customerId, requestId);
        return ResponseEntity.ok(ApiResponse.success(response, "Extension approved successfully"));
    }

    /**
     * Customer rejects extension
     */
    @PutMapping("/{requestId}/extend/reject")
    public ResponseEntity<ApiResponse<RequestExtensionResponse>> rejectExtension(
            Authentication authentication,
            @PathVariable Long requestId) {
        Long customerId = (Long) authentication.getPrincipal();
        log.info("Customer {} rejecting extension for request {}", customerId, requestId);
        RequestExtensionResponse response = extensionService.rejectExtension(customerId, requestId);
        return ResponseEntity.ok(ApiResponse.success(response, "Extension rejected"));
    }

    /**
     * Get extension history for a request
     */
    @GetMapping("/{requestId}/extensions")
    public ResponseEntity<ApiResponse<List<RequestExtensionResponse>>> getExtensions(
            @PathVariable Long requestId) {
        log.info("Getting extensions for request {}", requestId);
        List<RequestExtensionResponse> extensions = extensionService.getExtensions(requestId);
        return ResponseEntity.ok(ApiResponse.success(extensions, "Extensions retrieved successfully"));
    }
}
