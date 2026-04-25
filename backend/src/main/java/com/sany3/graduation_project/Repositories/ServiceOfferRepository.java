package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.ServiceOffer;
import com.sany3.graduation_project.entites.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ServiceOffer entity
 */
@Repository
public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, Long> {

    /**
     * Find all offers for a request
     * Customer sees these to choose from
     *
     * @param requestId Request ID
     * @param pageable Pagination
     * @return Page of offers
     */
    Page<ServiceOffer> findByRequestId(Long requestId, Pageable pageable);

    /**
     * Find all PENDING offers for a request
     *
     * @param requestId Request ID
     * @return List of pending offers (sorted by price)
     */
    @Query("SELECT so FROM ServiceOffer so " +
            "WHERE so.request.id = :requestId " +
            "AND so.status = 'PENDING' " +
            "ORDER BY so.offeredPrice ASC, so.createdAt ASC")
    List<ServiceOffer> findPendingOffersByRequest(@Param("requestId") Long requestId);

    /**
     * Find all offers by provider
     * @param providerId Provider ID
     * @param pageable Pagination
     * @return Page of offers
     */
    Page<ServiceOffer> findByProviderId(Long providerId, Pageable pageable);

    /**
     * Find PENDING offers by provider
     * Provider sees offers they made that are waiting
     *
     * @param providerId Provider ID
     * @param pageable Pagination
     * @return Page of pending offers
     */
    Page<ServiceOffer> findByProviderIdAndStatus(Long providerId,
                                                 OfferStatus status,
                                                 Pageable pageable);

    /**
     * Check if provider already made an offer for this request
     * Prevents duplicate offers
     *
     * @param requestId Request ID
     * @param providerId Provider ID
     * @return true if offer exists
     */
    boolean existsByRequestIdAndProviderId(Long requestId, Long providerId);

    /**
     * Find provider's offer for a request
     *
     * @param requestId Request ID
     * @param providerId Provider ID
     * @return Offer if exists
     */
    Optional<ServiceOffer> findByRequestIdAndProviderId(Long requestId, Long providerId);

    /**
     * Find accepted offer for a request
     * There should only be ONE accepted offer per request
     *
     * @param requestId Request ID
     * @return Accepted offer
     */
    @Query("SELECT so FROM ServiceOffer so " +
            "WHERE so.request.id = :requestId " +
            "AND so.status = 'ACCEPTED'")
    Optional<ServiceOffer> findAcceptedOfferByRequest(@Param("requestId") Long requestId);
}