package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.ServiceOfferRepository;
import com.sany3.graduation_project.Repositories.ServiceRequestRepository;
import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.dto.request.CreateServiceOfferRequest;
import com.sany3.graduation_project.entites.OfferStatus;
import com.sany3.graduation_project.entites.ServiceOffer;
import com.sany3.graduation_project.entites.ServiceRequest;
import com.sany3.graduation_project.entites.User;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageImpl;
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ServiceOfferService {

    private final ServiceOfferRepository serviceOfferRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;

    /**
     * Create a new offer
     * Provider sees request and submits an offer with price
     *
     * Validates:
     * - Provider exists
     * - Request exists
     * - Request is still OPEN
     * - Provider hasn't already offered
     *
     * @param providerId Provider ID
     * @param request Create offer DTO with requestId, price, ETA
     * @return Created offer
     */
    public ServiceOffer createOffer(Long providerId, CreateServiceOfferRequest request) {
        log.info("Creating offer from provider: {} for request: {}", providerId, request.getRequestId());

        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        ServiceRequest serviceRequest = serviceRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        // Check if provider already offered on this request
        if (serviceOfferRepository.existsByRequestIdAndProviderId(request.getRequestId(), providerId)) {
            throw new IllegalArgumentException("Provider already submitted an offer for this request");
        }

        // Check if request is still open
        if (!serviceRequest.getStatus().toString().equals("OPEN")) {
            throw new IllegalStateException("Request is no longer open");
        }

        ServiceOffer offer = ServiceOffer.builder()
                .request(serviceRequest)
                .provider(provider)
                .offeredPrice(request.getOfferedPrice())
                .estimatedTimeMinutes(request.getEstimatedTimeMinutes())
                .description(request.getDescription())
                .status(OfferStatus.PENDING)
                .build();

        offer = serviceOfferRepository.save(offer);
        log.info("Offer created with ID: {}", offer.getId());

        return offer;
    }

    /**
     * Get all offers for a request (paginated)
     * Customer sees all offers when choosing provider
     *
     * @param requestId Request ID
     * @param pageable Pagination
     * @return Page of offers (sorted by newest first)
     */
    public Page<ServiceOffer> getRequestOffers(Long requestId, Pageable pageable) {
        log.debug("Fetching offers for request: {}", requestId);
        return serviceOfferRepository.findByRequestIdOrderByCreatedAtDesc(requestId, pageable);
    }

    /**
     * Get all offers from a provider (paginated)
     * For provider's profile/dashboard
     *
     * @param providerId Provider ID
     * @param pageable Pagination
     * @return Page of offers
     */
    public Page<ServiceOffer> getProviderOffers(Long providerId, Pageable pageable) {
        log.debug("Fetching offers for provider: {}", providerId);
        return serviceOfferRepository.findByProviderId(providerId, pageable);
    }

    /**
     * Get pending offers for a provider (paginated)
     * Shows offers waiting for customer response
     *
     * @param providerId Provider ID
     * @param pageable Pagination
     * @return Page of pending offers
     */
    public Page<ServiceOffer> getPendingOffersByProvider(Long providerId, Pageable pageable) {
        log.debug("Fetching pending offers for provider: {}", providerId);

        // Get all offers from provider
        Page<ServiceOffer> allOffers = serviceOfferRepository.findByProviderId(providerId, pageable);

        // Filter manually since we need Page (not Streamable)
        java.util.List<ServiceOffer> pendingOffers = allOffers.getContent().stream()
                .filter(o -> o.getStatus().equals(OfferStatus.PENDING))
                .toList();

        // Return as Page
        return new org.springframework.data.domain.PageImpl<>(
                pendingOffers,
                pageable,
                allOffers.getTotalElements()
        );
    }

    /**
     * Get single offer details
     *
     * @param offerId Offer ID
     * @return Offer
     */
    public ServiceOffer getOfferById(Long offerId) {
        log.debug("Fetching offer: {}", offerId);
        return serviceOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
    }

    /**
     * Update offer (provider can update pending offer)
     * Can only update: price, ETA, description
     * Status cannot be changed here
     *
     * @param offerId Offer ID
     * @param request Updated offer data
     * @return Updated offer
     */
    public ServiceOffer updateOffer(Long offerId, CreateServiceOfferRequest request) {
        log.info("Updating offer: {}", offerId);

        ServiceOffer offer = getOfferById(offerId);

        // Can only update PENDING offers
        if (!offer.getStatus().equals(OfferStatus.PENDING)) {
            throw new IllegalStateException("Can only update pending offers");
        }

        offer.setOfferedPrice(request.getOfferedPrice());
        offer.setEstimatedTimeMinutes(request.getEstimatedTimeMinutes());
        offer.setDescription(request.getDescription());

        offer = serviceOfferRepository.save(offer);
        log.info("Offer updated: {}", offerId);

        return offer;
    }

    /**
     * Reject offer (customer rejects an offer)
     * Status changes from PENDING → REJECTED
     *
     * @param offerId Offer ID
     * @return Updated offer
     */
    public ServiceOffer rejectOffer(Long offerId) {
        log.info("Rejecting offer: {}", offerId);

        ServiceOffer offer = getOfferById(offerId);

        if (!offer.getStatus().equals(OfferStatus.PENDING)) {
            throw new IllegalStateException("Can only reject pending offers");
        }

        offer.setStatus(OfferStatus.REJECTED);
        offer.setRespondedAt(LocalDateTime.now());
        offer = serviceOfferRepository.save(offer);

        log.info("Offer rejected: {}", offerId);
        return offer;
    }

    /**
     * Withdraw offer (provider cancels their offer)
     * Status changes from PENDING → WITHDRAWN
     *
     * Validates:
     * - Offer is provider's own offer
     * - Offer is still PENDING
     *
     * @param offerId Offer ID
     * @param providerId Provider ID (for authorization)
     * @return Updated offer
     */
    public ServiceOffer withdrawOffer(Long offerId, Long providerId) {
        log.info("Provider {} withdrawing offer: {}", providerId, offerId);

        ServiceOffer offer = getOfferById(offerId);

        // Validate it's provider's offer
        if (!offer.getProvider().getId().equals(providerId)) {
            throw new IllegalArgumentException("You can only withdraw your own offers");
        }

        // Can only withdraw PENDING offers
        if (!offer.getStatus().equals(OfferStatus.PENDING)) {
            throw new IllegalStateException("Can only withdraw pending offers");
        }

        offer.setStatus(OfferStatus.WITHDRAWN);
        offer.setRespondedAt(LocalDateTime.now());
        offer = serviceOfferRepository.save(offer);

        log.info("Offer withdrawn: {}", offerId);
        return offer;
    }

    /**
     * Get count of pending offers for a request
     *
     * @param requestId Request ID
     * @return Count
     */
    public Long getPendingOfferCount(Long requestId) {
        log.debug("Counting pending offers for request: {}", requestId);
        return (long) serviceOfferRepository.findByRequestIdAndStatus(requestId, OfferStatus.PENDING).size();
    }

    /**
     * Get provider's accepted offers
     * Shows providers their active jobs
     *
     * @param providerId Provider ID
     * @return List of accepted offers
     */
    public java.util.List<ServiceOffer> getProviderAcceptedOffers(Long providerId) {
        log.debug("Fetching accepted offers for provider: {}", providerId);
        return serviceOfferRepository.findByProviderIdAndStatus(providerId, OfferStatus.ACCEPTED);
    }

    /**
     * Check if provider already offered on a request
     *
     * @param requestId Request ID
     * @param providerId Provider ID
     * @return true if offer exists
     */
    public Boolean hasProviderOffered(Long requestId, Long providerId) {
        log.debug("Checking if provider {} offered on request {}", providerId, requestId);
        return serviceOfferRepository.existsByRequestIdAndProviderId(requestId, providerId);
    }

    /**
     * Get average offer price for a request
     * Used for price comparison in UI
     *
     * @param requestId Request ID
     * @return Average price or 0.0 if no offers
     */
    public Double getAverageOfferPrice(Long requestId) {
        log.debug("Calculating average offer price for request: {}", requestId);
        Double avg = serviceOfferRepository.getAverageOfferPrice(requestId);
        return avg != null ? avg : 0.0;
    }

    /**
     * Get lowest priced offer for a request
     *
     * @param requestId Request ID
     * @return Lowest offer or null
     */
    public ServiceOffer getLowestOffer(Long requestId) {
        log.debug("Fetching lowest priced offer for request: {}", requestId);
        return serviceOfferRepository.findFirstByRequestIdOrderByOfferedPriceAsc(requestId).orElse(null);
    }

    /**
     * Get highest priced offer for a request
     *
     * @param requestId Request ID
     * @return Highest offer or null
     */
    public ServiceOffer getHighestOffer(Long requestId) {
        log.debug("Fetching highest priced offer for request: {}", requestId);
        return serviceOfferRepository.findFirstByRequestIdOrderByOfferedPriceDesc(requestId).orElse(null);
    }
}
