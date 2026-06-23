package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.ProviderDocumentRepository;
import com.sany3.graduation_project.Repositories.ServiceProviderProfileRepository;
import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.dto.request.ReapplyProviderRequest;
import com.sany3.graduation_project.dto.response.ProviderVerificationStatusResponse;
import com.sany3.graduation_project.entites.*;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import com.sany3.graduation_project.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provider-facing verification: see my own status, and re-apply after rejection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderVerificationService {

    private final ServiceProviderProfileRepository providerProfileRepository;
    private final UserRepository userRepository;
    private final ProviderDocumentRepository providerDocumentRepository;

    @Transactional(readOnly = true)
    public ProviderVerificationStatusResponse getMyStatus(Long userId) {
        ServiceProviderProfile profile = providerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider profile not found"));
        return toResponse(profile);
    }

    /**
     * A rejected provider edits their info/documents and resubmits.
     * Allowed only from REJECTED. Resets the profile back to PENDING and
     * clears the previous rejection reason.
     */
    @Transactional
    public ProviderVerificationStatusResponse reapply(Long userId, ReapplyProviderRequest request) {
        ServiceProviderProfile profile = providerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider profile not found"));

        if (!profile.isRejected()) {
            throw new IllegalStateException(
                    "You can only re-apply after your application was rejected. Current status: "
                            + profile.getVerificationStatus());
        }

        // Update only the fields the provider supplied
        if (request.getServiceType() != null) {
            profile.setServiceType(request.getServiceType());
        }
        if (request.getNationalId() != null && !request.getNationalId().equals(profile.getNationalId())) {
            if (providerProfileRepository.existsByNationalId(request.getNationalId())) {
                throw new UserAlreadyExistsException("National ID already registered");
            }
            profile.setNationalId(request.getNationalId());
        }
        if (request.getHourlyRate() != null) {
            profile.setHourlyRate(request.getHourlyRate());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getLatitude() != null) {
            profile.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            profile.setLongitude(request.getLongitude());
        }
        if (request.getHasCriminalRecord() != null) {
            profile.setHasCriminalRecord(request.getHasCriminalRecord());
        }

        // Updated profile picture lives on the User entity
        if (isNotBlank(request.getProfileImageUrl())) {
            User user = profile.getUser();
            user.setProfileImage(request.getProfileImageUrl().trim());
            userRepository.save(user);
        }

        // Updated criminal-history document => new ProviderDocument
        if (isNotBlank(request.getCriminalHistoryDocumentUrl())) {
            ProviderDocument document = new ProviderDocument();
            document.setServiceProviderProfile(profile);
            document.setDocumentType(DocumentType.CRIMINAL_HISTORY);
            document.setDocumentName("Criminal history document (re-applied)");
            document.setDocumentUrl(request.getCriminalHistoryDocumentUrl().trim());
            document.setIsVerified(false);
            providerDocumentRepository.save(document);
        }

        // Back to the review queue, clear the previous decision
        profile.setVerificationStatus(VerificationStatus.PENDING);
        profile.setIsVerified(false);
        profile.setRejectionReason(null);
        profile.setVerifiedByAdmin(null);
        profile.setVerificationDate(null);

        profile = providerProfileRepository.save(profile);
        log.info("Provider {} re-applied for verification (back to PENDING)", userId);

        return toResponse(profile);
    }

    private ProviderVerificationStatusResponse toResponse(ServiceProviderProfile profile) {
        return ProviderVerificationStatusResponse.builder()
                .verificationStatus(profile.getVerificationStatus().name())
                .isVerified(profile.getIsVerified())
                .rejectionReason(profile.getRejectionReason())
                .verificationDate(profile.getVerificationDate())
                .canReapply(profile.isRejected())
                .build();
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
