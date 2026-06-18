package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.Repositories.WalletRepository;
import com.sany3.graduation_project.Repositories.WalletTransactionRepository;
import com.sany3.graduation_project.dto.response.PaymentReceiptResponse;
import com.sany3.graduation_project.dto.response.StripePaymentIntentResponse;
import com.sany3.graduation_project.dto.response.WalletResponse;
import com.sany3.graduation_project.dto.response.WalletTransactionResponse;
import com.sany3.graduation_project.entites.*;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private final StripeService stripeService;

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

        log.info("Cash payment: provider {} owes {} for request {}", provider.getId(), fee, request.getId());
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

        log.info("Credit card payment: provider {} earned {} for request {}", provider.getId(), earning, request.getId());
        checkAndUpdateBanStatus(provider, wallet);
    }
    public PaymentReceiptResponse payPlatformFeeCash(Long providerId, BigDecimal amount) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        Wallet wallet = getOrCreateWallet(provider);

        validateFeePayment(wallet, amount);

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.FEE_PAYMENT)
                .amount(amount)
                .description("Platform fee payment (cash)")
                .build();
        walletTransactionRepository.save(transaction);

        log.info("Provider {} paid fee via cash: {}", providerId, amount);
        checkAndUpdateBanStatus(provider, wallet);

        return generateReceipt("FEE_PAYMENT", amount, provider, "Platform fee payment");
    }

    public StripePaymentIntentResponse payPlatformFeeCreditCard(Long providerId, BigDecimal amount) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        Wallet wallet = getOrCreateWallet(provider);

        validateFeePayment(wallet, amount);

        try {
            PaymentIntent intent = stripeService.createPaymentIntent(
                    amount, "egp", "Platform fee payment - " + provider.getName());

            WalletTransaction transaction = WalletTransaction.builder()
                    .wallet(wallet)
                    .type(TransactionType.FEE_PAYMENT)
                    .amount(amount)
                    .description("Platform fee payment (credit card) - PENDING")
                    .stripePaymentIntentId(intent.getId())
                    .status(PaymentStatus.PENDING)
                    .build();
            walletTransactionRepository.save(transaction);

            log.info("Provider {} fee payment PENDING: intent {}", providerId, intent.getId());

            return StripePaymentIntentResponse.builder()
                    .clientSecret(intent.getClientSecret())
                    .paymentIntentId(intent.getId())
                    .amount(amount)
                    .currency("egp")
                    .build();
        } catch (Exception e) {
            log.error("Stripe error for fee payment: {}", e.getMessage());
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }

    public void confirmFeePayment(Long providerId, String paymentIntentId) {
        WalletTransaction transaction = walletTransactionRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for intent: " + paymentIntentId));

        if (transaction.getStatus() == PaymentStatus.COMPLETED) {
            return;
        }

        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        Wallet wallet = getOrCreateWallet(provider);

        wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
        walletRepository.save(wallet);

        transaction.setStatus(PaymentStatus.COMPLETED);
        transaction.setDescription("Platform fee payment (credit card)");
        walletTransactionRepository.save(transaction);

        log.info("Provider {} fee payment CONFIRMED: {}", providerId, transaction.getAmount());
        checkAndUpdateBanStatus(provider, wallet);
    }

    // ── Provider Withdraws Earnings ──

    public PaymentReceiptResponse withdrawCash(Long providerId, BigDecimal amount) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));
        Wallet wallet = getOrCreateWallet(provider);

        validateWithdrawal(wallet, amount);

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(TransactionType.PAYOUT)
                .amount(amount.negate())
                .description("Withdrawal (cash)")
                .build();
        walletTransactionRepository.save(transaction);

        log.info("Provider {} withdrew via cash: {}", providerId, amount);

        return generateReceipt("WITHDRAWAL", amount, provider, "Earnings withdrawal");
    }


    // ── Admin Operations ──

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

    // ── Read Operations ──

    public WalletResponse getMyWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Wallet wallet = getOrCreateWallet(user);
        return toWalletResponse(wallet);
    }

    @Transactional(readOnly = true)
    public Page<WalletTransactionResponse> getMyTransactions(Long userId, Pageable pageable) {
        Wallet wallet = walletRepository.findByUserId(userId).orElse(null);
        if (wallet == null) {
            return Page.empty(pageable);
        }
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable)
                .map(this::toTransactionResponse);
    }

    // ── Private Helpers ──

    private void validateFeePayment(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) >= 0) {
            throw new IllegalStateException("You don't have any outstanding fees");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        BigDecimal maxPayable = wallet.getBalance().abs();
        if (amount.compareTo(maxPayable) > 0) {
            throw new IllegalArgumentException("Amount exceeds your outstanding fees. Maximum: " + maxPayable);
        }
    }

    private void validateWithdrawal(Wallet wallet, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No available balance to withdraw");
        }
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance. Available: " + wallet.getBalance());
        }
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

    private PaymentReceiptResponse generateReceipt(String type, BigDecimal amount, User provider, String description) {
        String referenceNumber = "SNY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return PaymentReceiptResponse.builder()
                .referenceNumber(referenceNumber)
                .type(type)
                .amount(amount)
                .providerName(provider.getName())
                .providerPhone(provider.getPhone())
                .description(description)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(48))
                .build();
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
