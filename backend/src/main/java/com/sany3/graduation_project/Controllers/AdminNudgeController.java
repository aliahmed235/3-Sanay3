package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.FirstRequestNudgeService;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.NudgeJobResultResponse;
import com.sany3.graduation_project.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/nudges")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminNudgeController {

    private final FirstRequestNudgeService nudgeService;

    /**
     * POST /admin/nudges/first-request/run-now — Manually trigger the nudge job
     */
    @PostMapping("/first-request/run-now")
    public ResponseEntity<ApiResponse<NudgeJobResultResponse>> triggerNudgeJob() {
        log.info("Admin triggered nudge job manually");

        NudgeJobResultResponse result = nudgeService.triggerNudgeJobManually();

        return ResponseEntity.ok(ApiResponse.success(result, Constants.SUCCESS_MESSAGE.NUDGE_JOB_COMPLETED));
    }
}
