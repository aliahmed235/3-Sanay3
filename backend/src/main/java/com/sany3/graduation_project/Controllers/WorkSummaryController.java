package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.WorkSummaryService;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.WorkSummaryResponse;
import com.sany3.graduation_project.mapper.WorkSummaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WorkSummaryController {

    private final WorkSummaryService workSummaryService;
    private final WorkSummaryMapper workSummaryMapper;

    /**
     * Add work summary after completing a job
     * POST /requests/{requestId}/work-summary
     * form-data: description (text), photos (files, optional)
     */
    @PostMapping(value = "/{requestId}/work-summary", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<WorkSummaryResponse>> addWorkSummary(
            @PathVariable Long requestId,
            @RequestParam("description") String description,
            @RequestPart(value = "beforePhotos", required = false) List<MultipartFile> beforePhotos,
            @RequestPart(value = "afterPhotos", required = false) List<MultipartFile> afterPhotos,
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        var result = workSummaryService.addWorkSummary(requestId, providerId, description, beforePhotos, afterPhotos);
        var response = workSummaryMapper.toWorkSummaryResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Work summary added"));
    }

    /**
     * Get work summary for a request
     * GET /requests/{requestId}/work-summary
     */
    @GetMapping("/{requestId}/work-summary")
    public ResponseEntity<ApiResponse<WorkSummaryResponse>> getWorkSummary(
            @PathVariable Long requestId) {

        var result = workSummaryService.getWorkSummary(requestId);

        if (result.getWorkSummary() == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No work summary for this request"));
        }

        var response = workSummaryMapper.toWorkSummaryResponse(result);
        return ResponseEntity.ok(ApiResponse.success(response, "Work summary retrieved"));
    }

    /**
     * Get provider's portfolio (all completed work with summaries)
     * GET /requests/provider/{providerId}/portfolio
     */
    @GetMapping("/provider/{providerId}/portfolio")
    public ResponseEntity<ApiResponse<List<WorkSummaryResponse>>> getProviderPortfolio(
            @PathVariable Long providerId) {

        List<WorkSummaryResponse> response = workSummaryService.getProviderPortfolio(providerId)
                .stream()
                .map(workSummaryMapper::toWorkSummaryResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response, "Portfolio retrieved"));
    }
}
