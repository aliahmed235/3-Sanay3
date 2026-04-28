package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.*;
import com.sany3.graduation_project.dto.request.CreateServiceRequestRequest;
import com.sany3.graduation_project.entites.*;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import com.sany3.graduation_project.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final ServiceOfferRepository serviceOfferRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RatingRepository ratingRepository;

    /**
     * Create a new service request
     * Automatically sets expiry to 24 hours from now
     *
     * @param customerId Customer ID
     * @param request Create request DTO
     * @return Created service request
     */
    public ServiceRequest createServiceRequest(Long customerId, CreateServiceRequestRequest request) {
        log.info("Creating service request for customer: {}", customerId);

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // Calculate expiry: 24 hours from now
        LocalDateTime expiryAt = LocalDateTime.now().plusHours(Constants.REQUEST.EXPIRY_HOURS);

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .customer(customer)
                .serviceType(request.getServiceType())
                .title(request.getTitle())
                .description(request.getDescription())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .status(RequestStatus.OPEN)
                .expiresAt(expiryAt)
                .build();

        serviceRequest = serviceRequestRepository.save(serviceRequest);
        log.info("Service request created with ID: {}", serviceRequest.getId());

        return serviceRequest;
    }

    /**
     * Get customer's requests
     *
     * @param customerId Customer ID
     * @param pageable Pagination
     * @return Page of requests
     */
    public Page<ServiceRequest> getCustomerRequests(Long customerId, Pageable pageable) {
        log.debug("Fetching requests for customer: {}", customerId);
        return serviceRequestRepository.findByCustomerId(customerId, pageable);
    }

    /**
     * Get open requests by service type
     * Used to show providers available work
     *
     * @param serviceType Service type filter
     * @param pageable Pagination
     * @return Page of open requests
     */
    public Page<ServiceRequest> getOpenRequestsByType(ServiceType serviceType, Pageable pageable) {
        log.debug("Fetching open requests for service type: {}", serviceType);
        return serviceRequestRepository.findByServiceTypeAndStatus(
                serviceType,
                RequestStatus.OPEN,
                pageable
        );
    }

    /**
     * Get nearby open requests for provider (geolocation)
     * Providers see requests on their map
     *
     * @param latitude Provider's latitude
     * @param longitude Provider's longitude
     * @param serviceType Filter by service type
     * @return List of nearby requests
     */
    public List<ServiceRequest> getNearbyRequests(BigDecimal latitude, BigDecimal longitude, ServiceType serviceType) {
        log.debug("Finding nearby requests for location: {}, {}", latitude, longitude);

        Double radiusKm = Constants.SEARCH.DEFAULT_SEARCH_RADIUS_KM;

        return serviceRequestRepository.findOpenRequestsByServiceTypeNearby(
                serviceType.toString(),
                latitude,
                longitude,
                radiusKm
        );
    }

    /**
     * Get single request details
     *
     * @param requestId Request ID
     * @return Service request
     */
    public ServiceRequest getRequestById(Long requestId) {
        log.debug("Fetching request: {}", requestId);
        return serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
    }

    /**
     * Accept an offer for this request
     * Updates request status, sets accepted provider, creates chat room
     *
     * @param requestId Request ID
     * @param offerId Offer ID to accept
     * @return Updated request
     */
    public ServiceRequest acceptOffer(Long requestId, Long offerId) {
        log.info("Accepting offer {} for request {}", offerId, requestId);

        ServiceRequest request = getRequestById(requestId);
        ServiceOffer offer = serviceOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        // Validate offer belongs to this request
        if (!offer.getRequest().getId().equals(requestId)) {
            throw new IllegalArgumentException("Offer does not belong to this request");
        }

        // Update request
        request.setAcceptedProvider(offer.getProvider());
        request.setAcceptedAt(LocalDateTime.now());
        request.setStatus(RequestStatus.ACCEPTED);
        request = serviceRequestRepository.save(request);

        // Update offer status
        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());
        serviceOfferRepository.save(offer);

        // Reject all other offers for this request
        List<ServiceOffer> otherOffers = serviceOfferRepository.findPendingOffersByRequest(requestId);
        for (ServiceOffer otherOffer : otherOffers) {
            if (!otherOffer.getId().equals(offerId)) {
                otherOffer.setStatus(OfferStatus.REJECTED);
                otherOffer.setRespondedAt(LocalDateTime.now());
                serviceOfferRepository.save(otherOffer);
            }
        }

        // Create chat room
        createChatRoomForRequest(request, offer.getProvider());

        log.info("Offer accepted. Request status: ACCEPTED");
        return request;
    }

    /**
     * Start service
     * Provider clicks "Start Service" button
     * Updates status to ONGOING
     *
     * @param requestId Request ID
     * @return Updated request
     */
    public ServiceRequest startService(Long requestId) {
        log.info("Starting service for request: {}", requestId);

        ServiceRequest request = getRequestById(requestId);

        if (!request.getStatus().equals(RequestStatus.ACCEPTED)) {
            throw new IllegalStateException("Request must be ACCEPTED before starting");
        }

        request.setStatus(RequestStatus.ONGOING);
        request.setStartedAt(LocalDateTime.now());
        request = serviceRequestRepository.save(request);

        log.info("Service started for request: {}", requestId);
        return request;
    }

    /**
     * Complete service
     * Provider clicks "Finish Service" button
     * Updates status to COMPLETED
     *
     * @param requestId Request ID
     * @return Updated request
     */
    public ServiceRequest completeService(Long requestId) {
        log.info("Completing service for request: {}", requestId);

        ServiceRequest request = getRequestById(requestId);

        if (!request.getStatus().equals(RequestStatus.ONGOING)) {
            throw new IllegalStateException("Request must be ONGOING before completing");
        }

        request.setStatus(RequestStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());
        request = serviceRequestRepository.save(request);

        log.info("Service completed for request: {}", requestId);
        return request;
    }
    /**
     * Cancel service request
     *
     * @param requestId Request ID
     * @return Updated request
     */
    public ServiceRequest cancelRequest(Long requestId) {
        log.info("Cancelling request: {}", requestId);

        ServiceRequest request = getRequestById(requestId);
        request.setStatus(RequestStatus.CANCELLED);
        request = serviceRequestRepository.save(request);

        log.info("Request cancelled: {}", requestId);
        return request;
    }

    /**
     * Auto-expire old OPEN requests
     * Scheduled to run every 5 minutes
     * Cancels requests that have expired
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000) // 5 minutes
    public void expireOldRequests() {
        log.debug("Running scheduled task: expire old requests");

        List<ServiceRequest> expiredRequests = serviceRequestRepository.findExpiredOpenRequests();

        for (ServiceRequest request : expiredRequests) {
            log.info("Expiring request: {}", request.getId());
            request.setStatus(RequestStatus.CANCELLED);
            serviceRequestRepository.save(request);
        }

        log.debug("Expired {} requests", expiredRequests.size());
    }

    /**
     * Create chat room when offer is accepted
     */
    private void createChatRoomForRequest(ServiceRequest request, User provider) {
        ChatRoom chatRoom = ChatRoom.builder()
                .request(request)
                .customer(request.getCustomer())
                .provider(provider)
                .build();

        chatRoomRepository.save(chatRoom);
        log.info("Chat room created for request: {}", request.getId());
    }
}