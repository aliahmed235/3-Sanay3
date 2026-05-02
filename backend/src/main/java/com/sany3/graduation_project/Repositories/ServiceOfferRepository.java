package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.OfferStatus;
import com.sany3.graduation_project.entites.ServiceOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, Long> {

    /**
     * Get all offers for a specific request
     */
    List<ServiceOffer> findByRequestId(Long requestId);

    /**
     * Get all offers from a provider (sorted by newest)
     */
    Page<ServiceOffer> findByProviderId(Long providerId, Pageable pageable);

    /**
     * Get all offers for a request paginated and sorted by newest first
     */
    Page<ServiceOffer> findByRequestIdOrderByCreatedAtDesc(Long requestId, Pageable pageable);

    /**
     * Get open offers for a request (PENDING status)
     */
    List<ServiceOffer> findByRequestIdAndStatus(Long requestId, OfferStatus status);

    /**
     * Check if provider already offered on a request
     * Returns true if exists, false otherwise
     */
    Boolean existsByRequestIdAndProviderId(Long requestId, Long providerId);

    /**
     * Get accepted offer for a request
     */
    Optional<ServiceOffer> findByRequestIdAndStatusOrderByCreatedAtAsc(Long requestId, OfferStatus status);

    /**
     * Count total offers by provider
     */
    Long countByProviderId(Long providerId);

    /**
     * Get provider's accepted offers (active jobs)
     */
    List<ServiceOffer> findByProviderIdAndStatus(Long providerId, OfferStatus status);

    /**
     * Get all pending offers for a request
     * Used when rejecting/accepting offers
     */
    @Query("SELECT so FROM ServiceOffer so WHERE so.request.id = :requestId AND so.status = 'PENDING'")
    List<ServiceOffer> findPendingOffersByRequest(@Param("requestId") Long requestId);

    /**
     * Get highest priced offer for a request
     */
    Optional<ServiceOffer> findFirstByRequestIdOrderByOfferedPriceDesc(Long requestId);

    /**
     * Get lowest priced offer for a request
     */
    Optional<ServiceOffer> findFirstByRequestIdOrderByOfferedPriceAsc(Long requestId);

    /**
     * Get average offer price for a request
     */
    @Query("SELECT AVG(so.offeredPrice) FROM ServiceOffer so WHERE so.request.id = :requestId")
    Double getAverageOfferPrice(@Param("requestId") Long requestId);
}
