package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.CloudinaryStorageService;
import com.sany3.graduation_project.Services.ServiceRequestService;
import com.sany3.graduation_project.dto.request.CreateServiceRequestRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.ServiceRequestResponse;
import com.sany3.graduation_project.entites.ServiceRequest;
import com.sany3.graduation_project.entites.ServiceType;
import com.sany3.graduation_project.mapper.ServiceRequestMapper;
import com.sany3.graduation_project.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;
    private final ServiceRequestMapper serviceRequestMapper;
    private final CloudinaryStorageService cloudinaryStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> createRequest(
            @Valid @ModelAttribute CreateServiceRequestRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Authentication authentication) {

        Long customerId = (Long) authentication.getPrincipal();

        String photoUrl = null;
        if (photo != null && !photo.isEmpty()) {
            photoUrl = cloudinaryStorageService.upload(photo, "requests");
        }

        ServiceRequest result = serviceRequestService.createServiceRequest(customerId, request, photoUrl);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        serviceRequestMapper.toServiceRequestResponse(result),
                        Constants.SUCCESS_MESSAGE.REQUEST_CREATED));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<Page<ServiceRequestResponse>>> getMyRequests(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long customerId = (Long) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceRequestResponse> response = serviceRequestService
                .getCustomerRequests(customerId, pageable)
                .map(serviceRequestMapper::toServiceRequestResponse); // ✅

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.RESOURCE_RETRIEVED));
    }

    @GetMapping("/open/{serviceType}")
    public ResponseEntity<ApiResponse<Page<ServiceRequestResponse>>> getOpenRequests(
            @PathVariable ServiceType serviceType,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long providerId = (Long) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceRequestResponse> response = serviceRequestService
                .getOpenRequestsByType(serviceType, providerId, pageable)
                .map(serviceRequestMapper::toServiceRequestResponse);

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.RESOURCE_RETRIEVED));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<ServiceRequestResponse>>> getNearbyRequests(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam ServiceType serviceType,
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        List<ServiceRequestResponse> response = serviceRequestService
                .getNearbyRequests(latitude, longitude, serviceType, providerId)
                .stream()
                .map(serviceRequestMapper::toServiceRequestResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.RESOURCE_RETRIEVED));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> getRequest(
            @PathVariable Long requestId) {

        ServiceRequestResponse response = serviceRequestMapper
                .toServiceRequestResponse(serviceRequestService.getRequestById(requestId)); // ✅

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.RESOURCE_RETRIEVED));
    }

    @PutMapping("/{requestId}/accept-offer/{offerId}")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> acceptOffer(
            @PathVariable Long requestId,
            @PathVariable Long offerId,
            Authentication authentication) {

        ServiceRequestResponse response = serviceRequestMapper
                .toServiceRequestResponse(serviceRequestService.acceptOffer(requestId, offerId)); // ✅

        return ResponseEntity.ok(ApiResponse.success(response, Constants.SUCCESS_MESSAGE.OFFER_ACCEPTED));
    }

    @PutMapping("/{requestId}/start")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> startService(
            @PathVariable Long requestId,
            Authentication authentication) {

        ServiceRequestResponse response = serviceRequestMapper
                .toServiceRequestResponse(serviceRequestService.startService(requestId)); // ✅

        return ResponseEntity.ok(ApiResponse.success(response, "Service started"));
    }

    @PutMapping("/{requestId}/complete")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> completeService(
            @PathVariable Long requestId,
            Authentication authentication) {

        ServiceRequestResponse response = serviceRequestMapper
                .toServiceRequestResponse(serviceRequestService.completeService(requestId)); // ✅

        return ResponseEntity.ok(ApiResponse.success(response, "Service completed"));
    }

    @PutMapping("/{requestId}/cancel")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> cancelRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        ServiceRequestResponse response = serviceRequestMapper
                .toServiceRequestResponse(serviceRequestService.cancelRequest(requestId));

        return ResponseEntity.ok(ApiResponse.success(response, "Request cancelled"));
    }
}