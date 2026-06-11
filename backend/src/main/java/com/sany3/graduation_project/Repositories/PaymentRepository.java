package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByServiceRequestId(Long serviceRequestId);

    @EntityGraph(attributePaths = {"serviceRequest", "customer", "provider"})
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    boolean existsByServiceRequestId(Long serviceRequestId);
}
