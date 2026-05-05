package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.*;
import com.sany3.graduation_project.dto.request.CreateRatingRequest;
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

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;

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
    public Page<Rating> getProviderRatings(Long providerId, int page, int size) {
        log.info("Fetching ratings for provider: {}", providerId);

        if (!userRepository.existsById(providerId)) {
            throw new ResourceNotFoundException("Provider not found");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be 1-100");
        }

        Pageable pageable = PageRequest.of(page, size);
        return ratingRepository.findByProviderId(providerId, pageable);
    }
    /**
     * Get rating for a specific request
     */
    public Rating getRatingForRequest(Long requestId) {
        log.debug("Fetching rating for request: {}", requestId);
        return ratingRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));
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
                .totalEarnings(0.0)  // TODO: Implement with payment table
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
}