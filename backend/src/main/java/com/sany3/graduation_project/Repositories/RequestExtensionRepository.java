package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.ExtensionStatus;
import com.sany3.graduation_project.entites.RequestExtension;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestExtensionRepository extends JpaRepository<RequestExtension, Long> {

    /**
     * Find pending extension for a request
     */
    Optional<RequestExtension> findByServiceRequestIdAndStatus(
            Long serviceRequestId, ExtensionStatus status);

    /**
     * Check if a pending extension already exists for a request
     */
    boolean existsByServiceRequestIdAndStatus(Long serviceRequestId, ExtensionStatus status);

    /**
     * Get all extensions for a request (history)
     */
    @EntityGraph(attributePaths = {"provider"})
    List<RequestExtension> findByServiceRequestIdOrderByCreatedAtDesc(Long serviceRequestId);
}
