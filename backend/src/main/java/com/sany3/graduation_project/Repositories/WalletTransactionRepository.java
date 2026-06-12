package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    java.util.Optional<WalletTransaction> findByStripePaymentIntentId(String stripePaymentIntentId);
}
