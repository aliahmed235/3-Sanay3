package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.*;
import com.sany3.graduation_project.dto.request.CreateRatingRequest;
import com.sany3.graduation_project.dto.response.ProviderAnalyticsDashboardResponse;
import com.sany3.graduation_project.dto.response.ProviderProfileStatsResponse;
import com.sany3.graduation_project.dto.response.ProviderRatingStats;
import com.sany3.graduation_project.entites.*;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import com.sany3.graduation_project.util.RatingCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final ServiceProviderProfileRepository providerProfileRepository;

    /**
     * Create rating after service completion
     * Applies penalty system automatically
     */
    public Rating createRating(Long customerId, Long requestId, CreateRatingRequest request) {
        log.info("Creating rating for request: {} by customer: {}", requestId, customerId);

        ServiceRequest serviceRequest = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!serviceRequest.getStatus().equals(RequestStatus.COMPLETED)) {
            throw new IllegalStateException("Can only rate completed services");
        }

        if (!serviceRequest.getCustomer().getId().equals(customerId)) {
            throw new IllegalStateException("Only the customer can rate this service");
        }

        if (serviceRequest.getRating() != null) {
            throw new IllegalStateException("This service has already been rated");
        }

        User provider = serviceRequest.getAcceptedProvider();

        Rating rating = Rating.builder()
                .request(serviceRequest)
                .customer(serviceRequest.getCustomer())
                .provider(provider)
                .ratingValue(request.getRatingValue())
                .review(request.getReview())
                .cancellationPenalty(0.0)
                .lateArrivalPenalty(request.getLateArrivalPenalty() != null ? request.getLateArrivalPenalty() : 0.0)
                .minutesLate(request.getMinutesLate())
                .incompleteServicePenalty(request.getIncompleteServicePenalty() != null ? request.getIncompleteServicePenalty() : 0.0)
                .incompleteServiceReason(request.getIncompleteServiceReason())
                .totalPenalty(0.0)
                .finalRating(0.0)
                .build();

        double totalPenalty = RatingCalculator.calculateTotalPenalty(rating);
        rating.setTotalPenalty(totalPenalty);

        double finalRating = RatingCalculator.calculateFinalRating(rating);
        rating.setFinalRating(finalRating);

        rating = ratingRepository.save(rating);
        log.info("Rating created with ID: {}, Final Rating: {}", rating.getId(), finalRating);

        return rating;
    }

    /**
     * Get all ratings for a provider
     */
    @Transactional(readOnly = true)
    public Page<Rating> getProviderRatings(Long providerId, int page, int size) {
        log.info("Fetching ratings for provider: {}", providerId);

        if (page < 0) throw new IllegalArgumentException("Page must be >= 0");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Size must be 1-100");

        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));


        Pageable pageable = PageRequest.of(page, size);
        return ratingRepository.findByProviderId(providerId, pageable);
    }
    /**
     * Get rating for a specific request
     */
    @Transactional(readOnly = true)
    public Rating getRatingForRequest(Long requestId) {
        log.debug("Fetching rating for request: {}", requestId);

        // ✅ VALIDATION 1: Check request exists
        if (!serviceRequestRepository.existsById(requestId)) {
            throw new ResourceNotFoundException("Request not found");
        }

        // ✅ VALIDATION 2: Fetch with proper error
        return ratingRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No rating found for request: " + requestId));
    }
    /**
     * Get average rating for a provider
     */
    public Double getProviderAverageRating(Long providerId) {
        log.debug("Calculating average rating for provider: {}", providerId);
        return ratingRepository.calculateAverageRating(providerId);
    }

    /**
     * Get rating count for a provider
     */
    public Long getProviderRatingCount(Long providerId) {
        log.debug("Counting ratings for provider: {}", providerId);
        return ratingRepository.countByProviderId(providerId);
    }

    /**
     * Get provider statistics
     */
    public ProviderRatingStats getProviderStats(Long providerId) {
        log.debug("Fetching stats for provider: {}", providerId);

        Double avgRating = getProviderAverageRating(providerId);
        Long ratingCount = getProviderRatingCount(providerId);

        return ProviderRatingStats.builder()
                .providerId(providerId)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalRatings(ratingCount != null ? ratingCount : 0L)
                .totalEarnings(0.0)
                .build();
    }

    /**
     * Get penalty counts for provider (for transparency)
     */
    public Long getCancellationPenaltyCount(Long providerId) {
        return ratingRepository.countCancellationPenalties(providerId);
    }

    public Long getLateArrivalPenaltyCount(Long providerId) {
        return ratingRepository.countLateArrivalPenalties(providerId);
    }

    public Long getIncompleteServicePenaltyCount(Long providerId) {
        return ratingRepository.countIncompleteServicePenalties(providerId);
    }

    // ══════════════════════════════════════════════════════════
    //  PROVIDER PROFILE STATS (Customer-facing)
    // ══════════════════════════════════════════════════════════

    /**
     * Get provider profile stats — shown to CUSTOMERS viewing a provider's profile.
     * Includes rating distribution, completion rate, on-time rate.
     */
    @Transactional(readOnly = true)
    public ProviderProfileStatsResponse getProviderProfileStats(Long providerId) {
        log.info("Building profile stats for provider: {}", providerId);

        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        // ── Rating stats ──
        Double avgRating = ratingRepository.calculateAverageRating(providerId);
        Long totalRatings = ratingRepository.countByProviderId(providerId);

        // ── Rating distribution (1-5 stars) ──
        Map<String, ProviderProfileStatsResponse.StarDistribution> distribution = buildRatingDistribution(providerId, totalRatings);

        // ── Request completion stats ──
        Long totalAssigned = serviceRequestRepository.countByAcceptedProviderId(providerId);
        Long completed = serviceRequestRepository.countCompletedByProviderId(providerId);
        Double completionRate = totalAssigned > 0
                ? Math.round((completed * 100.0 / totalAssigned) * 10.0) / 10.0
                : 0.0;

        // ── On-time stats ──
        Long lateCount = ratingRepository.countLateRatings(providerId);
        Double onTimeRate = totalRatings > 0
                ? Math.round(((totalRatings - lateCount) * 100.0 / totalRatings) * 10.0) / 10.0
                : 100.0;

        // ── Reputation label ──
        String reputationLabel = getReputationLabel(avgRating);

        return ProviderProfileStatsResponse.builder()
                .providerId(providerId)
                .providerName(provider.getName())
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
                .totalRatings(totalRatings != null ? totalRatings : 0L)
                .reputationLabel(reputationLabel)
                .ratingDistribution(distribution)
                .completionRate(completionRate)
                .onTimeRate(onTimeRate)
                .totalCompletedRequests(completed != null ? completed : 0L)
                .build();
    }

    // ══════════════════════════════════════════════════════════
    //  PROVIDER ANALYTICS DASHBOARD (Provider-facing)
    // ══════════════════════════════════════════════════════════

    /**
     * Get provider analytics dashboard — shown to the PROVIDER on their own dashboard.
     * Contains everything from profile stats PLUS detailed breakdowns.
     */
    @Transactional(readOnly = true)
    public ProviderAnalyticsDashboardResponse getProviderAnalyticsDashboard(Long providerId) {
        log.info("Building analytics dashboard for provider: {}", providerId);

        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        // ── Rating stats ──
        Double avgRating = ratingRepository.calculateAverageRating(providerId);
        Long totalRatings = ratingRepository.countByProviderId(providerId);
        Map<String, ProviderProfileStatsResponse.StarDistribution> distribution = buildRatingDistribution(providerId, totalRatings);

        // ── Request stats ──
        Long totalAssigned = serviceRequestRepository.countByAcceptedProviderId(providerId);
        Long completed = serviceRequestRepository.countCompletedByProviderId(providerId);
        Long cancelled = serviceRequestRepository.countCancelledByProviderId(providerId);
        Double completionRate = totalAssigned > 0
                ? Math.round((completed * 100.0 / totalAssigned) * 10.0) / 10.0
                : 0.0;

        // ── Latency stats ──
        Long lateCount = ratingRepository.countLateRatings(providerId);
        Double avgMinutesLate = ratingRepository.getAverageMinutesLate(providerId);
        Double onTimeRate = totalRatings > 0
                ? Math.round(((totalRatings - lateCount) * 100.0 / totalRatings) * 10.0) / 10.0
                : 100.0;

        // ── Penalty stats ──
        Long incompleteCount = ratingRepository.countIncompleteServicePenalties(providerId);
        Long cancellationPenaltyCount = ratingRepository.countCancellationPenalties(providerId);

        // ── Detailed breakdowns ──
        List<ProviderAnalyticsDashboardResponse.RatingDetail> recentRatings = buildRecentRatings(providerId);
        List<ProviderAnalyticsDashboardResponse.LateRequestDetail> lateRequests = buildLateRequests(providerId);
        List<ProviderAnalyticsDashboardResponse.IncompleteRequestDetail> incompleteRequests = buildIncompleteRequests(providerId);

        String reputationLabel = getReputationLabel(avgRating);

        return ProviderAnalyticsDashboardResponse.builder()
                .providerId(providerId)
                .providerName(provider.getName())
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
                .totalRatings(totalRatings != null ? totalRatings : 0L)
                .reputationLabel(reputationLabel)
                .ratingDistribution(distribution)
                .totalAssignedRequests(totalAssigned != null ? totalAssigned : 0L)
                .completedRequests(completed != null ? completed : 0L)
                .cancelledRequests(cancelled != null ? cancelled : 0L)
                .completionRate(completionRate)
                .onTimeRate(onTimeRate)
                .lateArrivals(lateCount != null ? lateCount : 0L)
                .averageMinutesLate(avgMinutesLate != null ? Math.round(avgMinutesLate * 10.0) / 10.0 : 0.0)
                .incompleteServices(incompleteCount != null ? incompleteCount : 0L)
                .cancellationPenalties(cancellationPenaltyCount != null ? cancellationPenaltyCount : 0L)
                .recentRatings(recentRatings)
                .lateRequests(lateRequests)
                .incompleteRequests(incompleteRequests)
                .build();
    }

    // ══════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════

    /**
     * Build rating distribution map (star 1-5 with count + percentage)
     */
    private Map<String, ProviderProfileStatsResponse.StarDistribution> buildRatingDistribution(
            Long providerId, Long totalRatings) {

        List<Object[]> rawDistribution = ratingRepository.getRatingDistribution(providerId);
        long total = (totalRatings != null && totalRatings > 0) ? totalRatings : 1; // avoid divide by zero

        // Initialize all stars with 0
        Map<String, ProviderProfileStatsResponse.StarDistribution> distribution = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            distribution.put(String.valueOf(star), ProviderProfileStatsResponse.StarDistribution.builder()
                    .count(0L)
                    .percentage(0.0)
                    .build());
        }

        // Fill in actual counts
        for (Object[] row : rawDistribution) {
            Integer starValue = (Integer) row[0];
            Long count = (Long) row[1];
            double percentage = Math.round((count * 100.0 / total) * 10.0) / 10.0;

            distribution.put(String.valueOf(starValue), ProviderProfileStatsResponse.StarDistribution.builder()
                    .count(count)
                    .percentage(percentage)
                    .build());
        }

        return distribution;
    }

    /**
     * Build recent ratings list (last 10) with full request details
     */
    private List<ProviderAnalyticsDashboardResponse.RatingDetail> buildRecentRatings(Long providerId) {
        List<Rating> ratings = ratingRepository.findRecentByProviderId(providerId, PageRequest.of(0, 10));

        return ratings.stream().map(r -> ProviderAnalyticsDashboardResponse.RatingDetail.builder()
                .ratingId(r.getId())
                .requestId(r.getRequest().getId())
                .requestTitle(r.getRequest().getTitle())
                .serviceType(r.getRequest().getServiceType().name())
                .ratingValue(r.getRatingValue())
                .finalRating(r.getFinalRating())
                .review(r.getReview())
                .wasLate(r.getMinutesLate() != null && r.getMinutesLate() > 0)
                .minutesLate(r.getMinutesLate())
                .wasIncomplete(r.getIncompleteServicePenalty() > 0.0)
                .hadCancellationPenalty(r.getCancellationPenalty() > 0.0)
                .createdAt(r.getCreatedAt())
                .build()
        ).toList();
    }

    /**
     * Build late requests list with details
     */
    private List<ProviderAnalyticsDashboardResponse.LateRequestDetail> buildLateRequests(Long providerId) {
        List<Rating> lateRatings = ratingRepository.findLateArrivalRatings(providerId);

        return lateRatings.stream().map(r -> ProviderAnalyticsDashboardResponse.LateRequestDetail.builder()
                .requestId(r.getRequest().getId())
                .requestTitle(r.getRequest().getTitle())
                .serviceType(r.getRequest().getServiceType().name())
                .minutesLate(r.getMinutesLate())
                .lateArrivalPenalty(r.getLateArrivalPenalty())
                .completedAt(r.getRequest().getCompletedAt())
                .build()
        ).toList();
    }

    /**
     * Build incomplete service requests list with details
     */
    private List<ProviderAnalyticsDashboardResponse.IncompleteRequestDetail> buildIncompleteRequests(Long providerId) {
        List<Rating> incompleteRatings = ratingRepository.findIncompleteServiceRatings(providerId);

        return incompleteRatings.stream().map(r -> ProviderAnalyticsDashboardResponse.IncompleteRequestDetail.builder()
                .requestId(r.getRequest().getId())
                .requestTitle(r.getRequest().getTitle())
                .serviceType(r.getRequest().getServiceType().name())
                .incompleteServiceReason(r.getIncompleteServiceReason())
                .penalty(r.getIncompleteServicePenalty())
                .build()
        ).toList();
    }

    /**
     * Get reputation label from average rating
     */
    private String getReputationLabel(Double avgRating) {
        if (avgRating == null || avgRating == 0) return "No ratings yet";
        if (avgRating >= 4.5) return "Excellent";
        if (avgRating >= 4.0) return "Very Good";
        if (avgRating >= 3.5) return "Good";
        if (avgRating >= 3.0) return "Average";
        return "Needs Improvement";
    }
}