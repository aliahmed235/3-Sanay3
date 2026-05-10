package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.WorkPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkPhotoRepository extends JpaRepository<WorkPhoto, Long> {

    List<WorkPhoto> findByServiceRequestId(Long requestId);

    /**
     * Get all work photos by a provider (for portfolio)
     */
    @Query("SELECT wp FROM WorkPhoto wp WHERE wp.serviceRequest.acceptedProvider.id = :providerId ORDER BY wp.createdAt DESC")
    List<WorkPhoto> findByProviderId(@Param("providerId") Long providerId);
}
