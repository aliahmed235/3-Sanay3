package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.ServiceOfferRepository;
import com.sany3.graduation_project.Repositories.ServiceRequestRepository;
import com.sany3.graduation_project.Repositories.WorkPhotoRepository;
import com.sany3.graduation_project.entites.OfferStatus;
import com.sany3.graduation_project.entites.ServiceRequest;
import com.sany3.graduation_project.entites.WorkPhoto;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WorkSummaryService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceOfferRepository serviceOfferRepository;
    private final WorkPhotoRepository workPhotoRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    /**
     * Add work summary after completing a job
     * Checks that the offer status is COMPLETED
     */
    public ServiceRequest addWorkSummary(Long requestId, Long providerId,
                                          String description, List<MultipartFile> photos) {
        log.info("Adding work summary for request: {}", requestId);

        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (request.getAcceptedProvider() == null || !request.getAcceptedProvider().getId().equals(providerId)) {
            throw new IllegalArgumentException("Only the accepted provider can add work summary");
        }

        // Check offer status is COMPLETED
        var offer = serviceOfferRepository.findByRequestIdAndStatusOrderByCreatedAtAsc(requestId, OfferStatus.COMPLETED)
                .orElseThrow(() -> new IllegalStateException("Offer must be COMPLETED to add work summary"));

        request.setWorkSummary(description);
        request = serviceRequestRepository.save(request);

        if (photos != null) {
            for (MultipartFile photo : photos) {
                if (!photo.isEmpty()) {
                    String url = cloudinaryStorageService.upload(photo, "work/" + requestId);
                    WorkPhoto workPhoto = WorkPhoto.builder()
                            .serviceRequest(request)
                            .photoUrl(url)
                            .build();
                    workPhotoRepository.save(workPhoto);
                }
            }
        }

        log.info("Work summary added for request: {}", requestId);
        return request;
    }

    /**
     * Get work summary for a specific request
     */
    public ServiceRequest getWorkSummary(Long requestId) {
        log.debug("Fetching work summary for request: {}", requestId);
        return serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
    }

    /**
     * Get all completed work by a provider (portfolio)
     */
    public List<ServiceRequest> getProviderPortfolio(Long providerId) {
        log.debug("Fetching portfolio for provider: {}", providerId);
        return serviceRequestRepository.findProviderCompletedWithWorkSummary(providerId);
    }

    /**
     * Get work photos for a request
     */
    public List<WorkPhoto> getWorkPhotos(Long requestId) {
        return workPhotoRepository.findByServiceRequestId(requestId);
    }
}
