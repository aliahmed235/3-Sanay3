package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Repositories.ServiceRequestRepository;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.ServiceRequestResponse;
import com.sany3.graduation_project.mapper.ServiceRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/provider/schedule")
@RequiredArgsConstructor
@Slf4j
public class ProviderScheduleController {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestMapper serviceRequestMapper;

    /**
     * Get provider's jobs for a specific date
     * GET /provider/schedule?date=2026-06-15
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getScheduleForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        log.info("GET /provider/schedule?date={} - Provider: {}", date, providerId);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<ServiceRequestResponse> jobs = serviceRequestRepository
                .findProviderSchedule(providerId, startOfDay, endOfDay)
                .stream()
                .map(serviceRequestMapper::toServiceRequestResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(jobs, "Schedule retrieved"));
    }

    /**
     * Get today's schedule
     * GET /provider/schedule/today
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getTodaySchedule(
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        log.info("GET /provider/schedule/today - Provider: {}", providerId);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        List<ServiceRequestResponse> jobs = serviceRequestRepository
                .findProviderSchedule(providerId, startOfDay, endOfDay)
                .stream()
                .map(serviceRequestMapper::toServiceRequestResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(jobs, "Today's schedule retrieved"));
    }

    /**
     * Get all jobs grouped by date
     * GET /provider/schedule/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Map<LocalDate, List<ServiceRequestResponse>>>> getAllJobs(
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        log.info("GET /provider/schedule/all - Provider: {}", providerId);

        List<ServiceRequestResponse> allJobs = serviceRequestRepository
                .findProviderAllJobs(providerId)
                .stream()
                .map(serviceRequestMapper::toServiceRequestResponse)
                .toList();

        Map<LocalDate, List<ServiceRequestResponse>> grouped = allJobs.stream()
                .collect(java.util.stream.Collectors.groupingBy(job -> {
                    LocalDateTime time = job.getScheduledAt() != null
                            ? job.getScheduledAt()
                            : job.getAcceptedAt();
                    return time != null ? time.toLocalDate() : LocalDate.now();
                }));

        return ResponseEntity.ok(ApiResponse.success(grouped, "Full schedule retrieved"));
    }

    /**
     * Get all upcoming jobs (ACCEPTED/ONGOING)
     * GET /api/provider/schedule/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getUpcomingJobs(
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        log.info("GET /api/provider/schedule/upcoming - Provider: {}", providerId);

        List<ServiceRequestResponse> jobs = serviceRequestRepository
                .findProviderUpcomingJobs(providerId)
                .stream()
                .map(serviceRequestMapper::toServiceRequestResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(jobs, "Upcoming jobs retrieved"));
    }

    /**
     * Get dates that have jobs (for calendar dots)
     * GET /api/provider/schedule/dates
     */
    @GetMapping("/dates")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getJobDates(
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        log.info("GET /api/provider/schedule/dates - Provider: {}", providerId);

        List<LocalDate> dates = serviceRequestRepository.findProviderJobDates(providerId);

        return ResponseEntity.ok(ApiResponse.success(dates, "Job dates retrieved"));
    }
}
