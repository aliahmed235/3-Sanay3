package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.ServiceOfferService;
import com.sany3.graduation_project.dto.request.CreateServiceOfferRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.ServiceOfferResponse;
import com.sany3.graduation_project.mapper.ServiceOfferMapper;
import com.sany3.graduation_project.entites.OfferStatus;
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

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ServiceOfferController {

    private final ServiceOfferService serviceOfferService;
    private final ServiceOfferMapper serviceOfferMapper;

    /**
     * Submit an offer for a request
     * POST /api/offers
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceOfferResponse>> createOffer(
            @Valid @RequestBody CreateServiceOfferRequest request,
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        var offer = serviceOfferService.createOffer(providerId, request);
        var response = serviceOfferMapper.toServiceOfferResponse(offer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Offer submitted successfully"));
    }

    /**
     * Get all offers for a request
     * GET /api/offers/request/{requestId}
     */
    @GetMapping("/request/{requestId}")
    public ResponseEntity<ApiResponse<Page<ServiceOfferResponse>>> getOffersForRequest(
            @PathVariable Long requestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        var offers = serviceOfferService.getRequestOffers(requestId, pageable);
        var response = offers.map(serviceOfferMapper::toServiceOfferResponse);

        return ResponseEntity.ok(ApiResponse.success(response, "Offers retrieved"));
    }

    /**
     * Get my offers (provider dashboard)
     * GET /api/offers/my-offers
     */
    @GetMapping("/my-offers")
    public ResponseEntity<ApiResponse<Page<ServiceOfferResponse>>> getMyOffers(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long providerId = (Long) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        var offers = serviceOfferService.getProviderOffers(providerId, pageable);
        var response = offers.map(serviceOfferMapper::toServiceOfferResponse);

        return ResponseEntity.ok(ApiResponse.success(response, "Your offers retrieved"));
    }

    /**
     * Get single offer
     * GET /api/offers/{offerId}
     */
    @GetMapping("/{offerId}")
    public ResponseEntity<ApiResponse<ServiceOfferResponse>> getOffer(
            @PathVariable Long offerId) {

        var offer = serviceOfferService.getOfferById(offerId);
        var response = serviceOfferMapper.toServiceOfferResponse(offer);

        return ResponseEntity.ok(ApiResponse.success(response, "Offer retrieved"));
    }

    /**
     * Update offer
     * PUT /api/offers/{offerId}
     */
    @PutMapping("/{offerId}")
    public ResponseEntity<ApiResponse<ServiceOfferResponse>> updateOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody CreateServiceOfferRequest request,
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        var offer = serviceOfferService.updateOffer(offerId, request);
        var response = serviceOfferMapper.toServiceOfferResponse(offer);

        return ResponseEntity.ok(ApiResponse.success(response, "Offer updated"));
    }

    /**
     * Withdraw offer
     * DELETE /api/offers/{offerId}
     */
    @DeleteMapping("/{offerId}")
    public ResponseEntity<ApiResponse<Void>> withdrawOffer(
            @PathVariable Long offerId,
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        serviceOfferService.withdrawOffer(offerId, providerId);

        return ResponseEntity.ok(ApiResponse.success(null, "Offer withdrawn"));
    }
}