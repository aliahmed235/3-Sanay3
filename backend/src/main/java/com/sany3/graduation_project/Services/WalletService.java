package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.Repositories.WalletRepository;
import com.sany3.graduation_project.Repositories.WalletTransactionRepository;
import com.sany3.graduation_project.dto.response.WalletResponse;
import com.sany3.graduation_project.dto.response.WalletTransactionResponse;
import com.sany3.graduation_project.entites.*;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.15");
    private static final BigDecimal BAN_THRESHOLD = new BigDecimal("-1000");

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;

    public Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Wallet wallet = Wallet.builder()
                            .user(user)
                            .balance(BigDecimal.ZERO)
                            .build();
                    return walletRepository.save(wallet);
                });
    }

    public void processCashPayment(User provider, ServiceRequest request, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(provider);

        BigDecimal fee = amount.multiply(PLATFORM_FEE_RATE).negate();

        wallet.setBalance(wallet.getBalance().add(fee));
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.PLATFORM_FEE)
                .amount(fee)
                .description("Platform fee 15% - " + request.getTitle())
                .serviceRequest(request)
                .build();
        walletTransactionRepository.save(transaction);

        log.info("Cash payment processed: provider {} owes {} for request {}", provider.getId(), fee, request.getId());

        checkAndUpdateBanStatus(provider, wallet);
    }

    public void processCreditCardPayment(User provider, ServiceRequest request, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(provider);

        BigDecimal earning = amount.subtract(amount.multiply(PLATFORM_FEE_RATE));

        wallet.setBalance(wallet.getBalance().add(earning));
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.CREDIT_CARD_EARNING)
                .amount(earning)
                .description("Credit card earning (85%) - " + request.getTitle())
                .serviceRequest(request)
                .build();
        walletTransactionRepository.save(transaction);

        log.info("Credit card payment processed: provider {} earned {} for request {}", provider.getId(), earning, request.getId());
    }

    public void payPlatformFee(Long providerId, BigDecimal amount) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        Wallet wallet = getOrCreateWallet(provider);

        if (wallet.getBalance().compareTo(BigDecimal.ZERO) >= 0) {
            throw new IllegalStateException("You don't have any outstanding fees");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.FEE_PAYMENT)
                .amount(amount)
                .description("Platform fee payment")
                .build();
        walletTransactionRepository.save(transaction);

        log.info("Provider {} paid platform fee: {}", providerId, amount);

        checkAndUpdateBanStatus(provider, wallet);
    }

    public void processWithdrawal(Long providerId, BigDecimal amount) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        Wallet wallet = getOrCreateWallet(provider);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance. Your balance is " + wallet.getBalance());
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.PAYOUT)
                .amount(amount.negate())
                .description("Withdrawal request")
                .build();
        walletTransactionRepository.save(transaction);

        log.info("Provider {} requested withdrawal: {}", providerId, amount);
    }

    public void processPayout(Long providerId, BigDecimal amount, String note) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        Wallet wallet = getOrCreateWallet(provider);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Provider balance is less than payout amount");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.PAYOUT)
                .amount(amount.negate())
                .description("Admin payout - " + (note != null ? note : ""))
                .build();
        walletTransactionRepository.save(transaction);

        log.info("Admin processed payout for provider {}: {}", providerId, amount);
    }

    public WalletResponse getMyWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Wallet wallet = getOrCreateWallet(user);
        return toWalletResponse(wallet);
    }

    @Transactional(readOnly = true)
    public Page<WalletTransactionResponse> getMyTransactions(Long userId, Pageable pageable) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElse(null);
        if (wallet == null) {
            return Page.empty(pageable);
        }
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable)
                .map(this::toTransactionResponse);
    }

    private void checkAndUpdateBanStatus(User provider, Wallet wallet) {
        boolean shouldBeBanned = wallet.getBalance().compareTo(BAN_THRESHOLD) <= 0;

        if (shouldBeBanned && !provider.getBanned()) {
            provider.setBanned(true);
            userRepository.save(provider);
            log.warn("Provider {} BANNED - balance {} exceeded threshold {}", provider.getId(), wallet.getBalance(), BAN_THRESHOLD);
        } else if (!shouldBeBanned && provider.getBanned()) {
            provider.setBanned(false);
            userRepository.save(provider);
            log.info("Provider {} UNBANNED - balance {} is above threshold", provider.getId(), wallet.getBalance());
        }
    }

    private WalletResponse toWalletResponse(Wallet wallet) {
        String status;
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            status = "POSITIVE";
        } else if (wallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            status = "NEGATIVE";
        } else {
            status = "ZERO";
        }

        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUser().getId())
                .userName(wallet.getUser().getName())
                .balance(wallet.getBalance())
                .balanceStatus(status)
                .createdAt(wallet.getCreatedAt())
                .build();
    }

    private WalletTransactionResponse toTransactionResponse(WalletTransaction tx) {
        return WalletTransactionResponse.builder()
                .id(tx.getId())
                .type(tx.getType().name())
                .typeDisplayName(tx.getType().getDisplayName())
                .amount(tx.getAmount())
                .description(tx.getDescription())
                .serviceRequestId(tx.getServiceRequest() != null ? tx.getServiceRequest().getId() : null)
                .serviceRequestTitle(tx.getServiceRequest() != null ? tx.getServiceRequest().getTitle() : null)
                .createdAt(tx.getCreatedAt())
                .build();
    }
}