package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.RequestExtensionRepository;
import com.sany3.graduation_project.Repositories.ServiceRequestRepository;
import com.sany3.graduation_project.dto.request.RequestExtensionRequest;
import com.sany3.graduation_project.dto.response.RequestExtensionResponse;
import com.sany3.graduation_project.entites.*;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestExtensionService {

    private final ServiceRequestRepository requestRepository;
    private final RequestExtensionRepository extensionRepository;

    /**
     * Provider requests extension — needs more days and/or updated price
     * Request must be ONGOING, and provider must be the accepted provider
     */
    @Transactional
    public RequestExtensionResponse requestExtension(Long providerId, Long requestId,
                                                      RequestExtensionRequest request) {
        ServiceRequest serviceRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        // Validate: must be the accepted provider
        if (serviceRequest.getAcceptedProvider() == null ||
                !serviceRequest.getAcceptedProvider().getId().equals(providerId)) {
            throw new IllegalStateException("You are not the accepted provider for this request");
        }

        // Validate: request must be ONGOING
        if (serviceRequest.getStatus() != RequestStatus.ONGOING) {
            throw new IllegalStateException("Extension can only be requested for ongoing services");
        }

        // Validate: no pending extension already exists
        if (extensionRepository.existsByServiceRequestIdAndStatus(requestId, ExtensionStatus.PENDING)) {
            throw new IllegalStateException("A pending extension already exists for this request");
        }

        RequestExtension extension = RequestExtension.builder()
                .serviceRequest(serviceRequest)
                .provider(serviceRequest.getAcceptedProvider())
                .additionalDays(request.getAdditionalDays())
                .originalPrice(getOriginalPrice(serviceRequest))
                .updatedPrice(request.getUpdatedPrice())
                .reason(request.getReason())
                .status(ExtensionStatus.PENDING)
                .build();

        extension = extensionRepository.save(extension);

        // Update request status
        serviceRequest.setStatus(RequestStatus.EXTENSION_REQUESTED);
        requestRepository.save(serviceRequest);

        log.info("Extension requested for request {} by provider {}: {} more days, new price {}",
                requestId, providerId, request.getAdditionalDays(), request.getUpdatedPrice());

        return toResponse(extension);
    }

    /**
     * Customer approves extension — price updated, status back to ONGOING
     */
    @Transactional
    public RequestExtensionResponse approveExtension(Long customerId, Long requestId) {
        ServiceRequest serviceRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        // Validate: must be the customer
        if (!serviceRequest.getCustomer().getId().equals(customerId)) {
            throw new IllegalStateException("Only the customer can approve extensions");
        }

        // Validate: must have EXTENSION_REQUESTED status
        if (serviceRequest.getStatus() != RequestStatus.EXTENSION_REQUESTED) {
            throw new IllegalStateException("No pending extension for this request");
        }

        RequestExtension extension = extensionRepository
                .findByServiceRequestIdAndStatus(requestId, ExtensionStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Extension not found"));

        // Approve
        extension.setStatus(ExtensionStatus.APPROVED);
        extension.setRespondedAt(LocalDateTime.now());
        extensionRepository.save(extension);

        // Update request status back to ONGOING
        serviceRequest.setStatus(RequestStatus.ONGOING);
        requestRepository.save(serviceRequest);

        log.info("Extension approved for request {} by customer {}", requestId, customerId);
        return toResponse(extension);
    }

    /**
     * Customer rejects extension — no changes, status back to ONGOING
     */
    @Transactional
    public RequestExtensionResponse rejectExtension(Long customerId, Long requestId) {
        ServiceRequest serviceRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        // Validate: must be the customer
        if (!serviceRequest.getCustomer().getId().equals(customerId)) {
            throw new IllegalStateException("Only the customer can reject extensions");
        }

        // Validate: must have EXTENSION_REQUESTED status
        if (serviceRequest.getStatus() != RequestStatus.EXTENSION_REQUESTED) {
            throw new IllegalStateException("No pending extension for this request");
        }

        RequestExtension extension = extensionRepository
                .findByServiceRequestIdAndStatus(requestId, ExtensionStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Extension not found"));

        // Reject
        extension.setStatus(ExtensionStatus.REJECTED);
        extension.setRespondedAt(LocalDateTime.now());
        extensionRepository.save(extension);

        // Status back to ONGOING (no price change)
        serviceRequest.setStatus(RequestStatus.ONGOING);
        requestRepository.save(serviceRequest);

        log.info("Extension rejected for request {} by customer {}", requestId, customerId);
        return toResponse(extension);
    }

    /**
     * Get extension details for a request
     */
    @Transactional(readOnly = true)
    public List<RequestExtensionResponse> getExtensions(Long requestId) {
        return extensionRepository.findByServiceRequestIdOrderByCreatedAtDesc(requestId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private java.math.BigDecimal getOriginalPrice(ServiceRequest serviceRequest) {
        if (serviceRequest.getOffers() != null) {
            return serviceRequest.getOffers().stream()
                    .filter(o -> o.getStatus() == OfferStatus.ACCEPTED)
                    .map(o -> o.getOfferedPrice())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private RequestExtensionResponse toResponse(RequestExtension extension) {
        return RequestExtensionResponse.builder()
                .id(extension.getId())
                .serviceRequestId(extension.getServiceRequest().getId())
                .providerId(extension.getProvider().getId())
                .providerName(extension.getProvider().getName())
                .additionalDays(extension.getAdditionalDays())
                .originalPrice(extension.getOriginalPrice())
                .updatedPrice(extension.getUpdatedPrice())
                .reason(extension.getReason())
                .status(extension.getStatus())
                .respondedAt(extension.getRespondedAt())
                .createdAt(extension.getCreatedAt())
                .build();
    }
}
