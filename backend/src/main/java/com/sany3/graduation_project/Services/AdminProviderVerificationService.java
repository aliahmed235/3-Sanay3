package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.ServiceProviderProfileRepository;
import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.dto.response.VerificationStatsResponse;
import com.sany3.graduation_project.entites.ServiceProviderProfile;
import com.sany3.graduation_project.entites.User;
import com.sany3.graduation_project.entites.VerificationStatus;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminProviderVerificationService {

    private final ServiceProviderProfileRepository providerProfileRepository;
    private final UserRepository userRepository;

    /**
     * Get all pending provider profiles (paginated)
     */
    public Page<ServiceProviderProfile> getPendingProviders(Pageable pageable) {
        log.debug("Fetching pending providers");
        return providerProfileRepository.findByVerificationStatus(VerificationStatus.PENDING, pageable);
    }

    /**
     * Get provider details with documents
     */
    public ServiceProviderProfile getProviderDetails(Long profileId) {
        log.debug("Fetching provider details for profile: {}", profileId);
        return providerProfileRepository.findWithDetailsById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider profile not found"));
    }

    /**
     * Approve a provider
     * Sets isVerified=true, status=APPROVED, records admin and date
     */
    public ServiceProviderProfile approveProvider(Long profileId, Long adminUserId) {
        log.info("Admin {} approving provider profile {}", adminUserId, profileId);

        ServiceProviderProfile profile = providerProfileRepository.findWithDetailsById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider profile not found"));

        if (!profile.isPending()) {
            throw new IllegalStateException(
                    "Provider must be in PENDING status to approve. Current status: " + profile.getVerificationStatus());
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        profile.setIsVerified(true);
        profile.setVerificationStatus(VerificationStatus.APPROVED);
        profile.setVerifiedByAdmin(admin);
        profile.setVerificationDate(LocalDateTime.now());
        profile.setRejectionReason(null);

        profile = providerProfileRepository.save(profile);
        log.info("Provider profile {} approved by admin {}", profileId, adminUserId);

        return profile;
    }

    /**
     * Reject a provider
     * Sets status=REJECTED, records rejection reason, admin and date
     */
    public ServiceProviderProfile rejectProvider(Long profileId, Long adminUserId, String rejectionReason) {
        log.info("Admin {} rejecting provider profile {}", adminUserId, profileId);

        ServiceProviderProfile profile = providerProfileRepository.findWithDetailsById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider profile not found"));

        if (!profile.isPending()) {
            throw new IllegalStateException(
                    "Provider must be in PENDING status to reject. Current status: " + profile.getVerificationStatus());
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        profile.setIsVerified(false);
        profile.setVerificationStatus(VerificationStatus.REJECTED);
        profile.setVerifiedByAdmin(admin);
        profile.setVerificationDate(LocalDateTime.now());
        profile.setRejectionReason(rejectionReason);

        profile = providerProfileRepository.save(profile);
        log.info("Provider profile {} rejected by admin {}", profileId, adminUserId);

        return profile;
    }

    /**
     * Get verification statistics
     */
    public VerificationStatsResponse getVerificationStats() {
        log.debug("Fetching verification stats");

        long pendingCount = providerProfileRepository.countByVerificationStatus(VerificationStatus.PENDING);
        long approvedCount = providerProfileRepository.countByVerificationStatus(VerificationStatus.APPROVED);
        long rejectedCount = providerProfileRepository.countByVerificationStatus(VerificationStatus.REJECTED);

        return VerificationStatsResponse.builder()
                .pendingCount(pendingCount)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .totalCount(pendingCount + approvedCount + rejectedCount)
                .build();
    }
}
