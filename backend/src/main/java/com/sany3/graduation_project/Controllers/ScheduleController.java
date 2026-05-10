package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.ScheduleService;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.ScheduleDayResponse;
import com.sany3.graduation_project.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/provider/schedule")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleMapper scheduleMapper;

    /**
     * Get today's schedule (default view when opening schedule screen)
     * GET /provider/schedule/today
     * Shows all jobs for today with their statuses
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<ScheduleDayResponse>>> getTodaySchedule(
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        List<ScheduleDayResponse> response = scheduleService
                .getScheduleForDate(providerId, LocalDate.now())
                .stream()
                .map(scheduleMapper::toScheduleDayResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response, "Today's schedule retrieved"));
    }

    /**
     * Get dates that have jobs (for calendar highlighting)
     * GET /provider/schedule/dates
     * Returns: ["2026-05-10", "2026-05-12", "2026-05-15"]
     * Includes all statuses (accepted, ongoing, completed, cancelled)
     */
    @GetMapping("/dates")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getJobDates(
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        List<LocalDate> dates = scheduleService.getJobDates(providerId);

        return ResponseEntity.ok(ApiResponse.success(dates, "Job dates retrieved"));
    }

    /**
     * Get provider's schedule for a specific date
     * GET /provider/schedule?date=2026-05-10
     * Shows all jobs for that day with statuses (accepted, ongoing, completed, cancelled)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleDayResponse>>> getScheduleForDate(
            @RequestParam LocalDate date,
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        List<ScheduleDayResponse> response = scheduleService
                .getScheduleForDate(providerId, date)
                .stream()
                .map(scheduleMapper::toScheduleDayResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response, "Schedule retrieved"));
    }

    /**
     * Get all upcoming jobs (only ACCEPTED/ONGOING)
     * GET /provider/schedule/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<ScheduleDayResponse>>> getUpcomingJobs(
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        List<ScheduleDayResponse> response = scheduleService
                .getUpcomingJobs(providerId)
                .stream()
                .map(scheduleMapper::toScheduleDayResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response, "Upcoming jobs retrieved"));
    }

    /**
     * Get full schedule grouped by day (includes all statuses)
     * GET /provider/schedule/all
     * Returns: { "2026-05-10": [...], "2026-05-11": [...] }
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Map<String, List<ScheduleDayResponse>>>> getFullSchedule(
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        var jobs = scheduleService.getAllJobs(providerId);

        Map<String, List<ScheduleDayResponse>> grouped = jobs.stream()
                .collect(Collectors.groupingBy(
                        sr -> {
                            LocalDate date = sr.getScheduledAt() != null
                                    ? sr.getScheduledAt().toLocalDate()
                                    : sr.getAcceptedAt().toLocalDate();
                            return date.toString();
                        },
                        LinkedHashMap::new,
                        Collectors.mapping(scheduleMapper::toScheduleDayResponse, Collectors.toList())
                ));

        return ResponseEntity.ok(ApiResponse.success(grouped, "Full schedule retrieved"));
    }
}
