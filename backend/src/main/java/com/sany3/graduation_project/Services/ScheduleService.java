package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.ServiceRequestRepository;
import com.sany3.graduation_project.entites.ServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ServiceRequestRepository serviceRequestRepository;

    /**
     * Get dates that have jobs for a provider (for calendar highlighting)
     */
    public List<LocalDate> getJobDates(Long providerId) {
        log.debug("Fetching job dates for provider {}", providerId);
        return serviceRequestRepository.findProviderJobDates(providerId);
    }

    /**
     * Get provider's jobs for a specific date
     */
    public List<ServiceRequest> getScheduleForDate(Long providerId, LocalDate date) {
        log.debug("Fetching schedule for provider {} on {}", providerId, date);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return serviceRequestRepository.findProviderSchedule(providerId, startOfDay, endOfDay);
    }

    /**
     * Get all upcoming jobs for a provider (only ACCEPTED/ONGOING)
     */
    public List<ServiceRequest> getUpcomingJobs(Long providerId) {
        log.debug("Fetching upcoming jobs for provider {}", providerId);
        return serviceRequestRepository.findProviderUpcomingJobs(providerId);
    }

    /**
     * Get all jobs for a provider including completed and cancelled (for full schedule history)
     */
    public List<ServiceRequest> getAllJobs(Long providerId) {
        log.debug("Fetching all jobs for provider {}", providerId);
        return serviceRequestRepository.findProviderAllJobs(providerId);
    }
}
