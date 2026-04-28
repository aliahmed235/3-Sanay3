package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.ServiceRequestService;
import com.sany3.graduation_project.dto.request.CreateServiceRequestRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.entites.ServiceRequest;
import com.sany3.graduation_project.entites.ServiceType;
import com.sany3.graduation_project.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    /**
     * Create a new service request
     * POST /requests
     *
     * Request:
     * {
     *   "serviceType": "GAS",
     *   "title": "Gas connection not working",
     *   "description": "No gas in kitchen, need urgent fix",
     *   "address": "House #123, Clifton, Karachi",
     *   "latitude": 24.7898,
     *   "longitude": 67.0345,
     *   "budget": 1000
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceRequest>> createRequest(
            @Valid @RequestBody CreateServiceRequestRequest request,
            Authentication authentication) {
        log.info("Creating service request");

        Long customerId = (Long) authentication.getPrincipal();
        ServiceRequest response = serviceRequestService.createServiceRequest(customerId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.REQUEST_CREATED));
    }

    /**
     * Get my requests (customer)
     * GET /requests/my-requests?page=0&size=10
     *
     * Returns paginated list of customer's requests
     */
    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<Page<ServiceRequest>>> getMyRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching my requests");

        Long customerId = (Long) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceRequest> response = serviceRequestService.getCustomerRequests(customerId, pageable);

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.RESOURCE_RETRIEVED));
    }

    /**
     * Get open requests by service type (for providers)
     * GET /requests/open/GAS?page=0&size=10
     *
     * Providers browse available requests by type
     */
    @GetMapping("/open/{serviceType}")
    public ResponseEntity<ApiResponse<Page<ServiceRequest>>> getOpenRequests(
            @PathVariable ServiceType serviceType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching open requests for service type: {}", serviceType);

        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceRequest> response = serviceRequestService.getOpenRequestsByType(serviceType, pageable);

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.RESOURCE_RETRIEVED));
    }

    /**
     * Get nearby requests (for provider map)
     * GET /requests/nearby?latitude=24.7898&longitude=67.0345&serviceType=GAS
     *
     * Providers see requests near their location
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<ServiceRequest>>> getNearbyRequests(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam ServiceType serviceType) {
        log.info("Fetching nearby requests for location: {}, {}", latitude, longitude);

        List<ServiceRequest> response = serviceRequestService.getNearbyRequests(latitude, longitude, serviceType);

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.RESOURCE_RETRIEVED));
    }

    /**
     * Get request details
     * GET /requests/{requestId}
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<ServiceRequest>> getRequest(
            @PathVariable Long requestId) {
        log.info("Fetching request: {}", requestId);

        ServiceRequest response = serviceRequestService.getRequestById(requestId);

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.RESOURCE_RETRIEVED));
    }

    /**
     * Accept an offer
     * PUT /requests/{requestId}/accept-offer/{offerId}
     *
     * Customer accepts provider's offer → chat opens
     */
    @PutMapping("/{requestId}/accept-offer/{offerId}")
    public ResponseEntity<ApiResponse<ServiceRequest>> acceptOffer(
            @PathVariable Long requestId,
            @PathVariable Long offerId,
            Authentication authentication) {
        log.info("Accepting offer {} for request {}", offerId, requestId);

        Long customerId = (Long) authentication.getPrincipal();
        ServiceRequest response = serviceRequestService.acceptOffer(requestId, offerId);

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.OFFER_ACCEPTED));
    }

    /**
     * Start service
     * PUT /requests/{requestId}/start
     *
     * Provider clicks "Start Service" button
     * Status: ACCEPTED → ONGOING
     */
    @PutMapping("/{requestId}/start")
    public ResponseEntity<ApiResponse<ServiceRequest>> startService(
            @PathVariable Long requestId,
            Authentication authentication) {
        log.info("Starting service for request: {}", requestId);

        ServiceRequest response = serviceRequestService.startService(requestId);

        return ResponseEntity.ok(ApiResponse.success(response, "Service started"));
    }

    /**
     * Complete service
     * PUT /requests/{requestId}/complete
     *
     * Provider clicks "Finish Service" button
     * Status: ONGOING → COMPLETED (ready for rating)
     */
    @PutMapping("/{requestId}/complete")
    public ResponseEntity<ApiResponse<ServiceRequest>> completeService(
            @PathVariable Long requestId,
            Authentication authentication) {
        log.info("Completing service for request: {}", requestId);

        ServiceRequest response = serviceRequestService.completeService(requestId);

        return ResponseEntity.ok(ApiResponse.success(response, "Service completed"));
    }

    /**
     * Cancel request
     * PUT /requests/{requestId}/cancel
     */
    @PutMapping("/{requestId}/cancel")
    public ResponseEntity<ApiResponse<ServiceRequest>> cancelRequest(
            @PathVariable Long requestId,
            Authentication authentication) {
        log.info("Cancelling request: {}", requestId);

        ServiceRequest response = serviceRequestService.cancelRequest(requestId);

        return ResponseEntity.ok(ApiResponse.success(response, "Request cancelled"));
    }
}