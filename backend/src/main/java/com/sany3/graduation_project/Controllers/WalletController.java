package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.WalletService;
import com.sany3.graduation_project.dto.request.PayFeeRequest;
import com.sany3.graduation_project.dto.request.WithdrawRequest;
import com.sany3.graduation_project.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<WalletResponse>> getMyWallet(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        WalletResponse response = walletService.getMyWallet(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Wallet retrieved"));
    }

    @GetMapping("/my/transactions")
    public ResponseEntity<ApiResponse<Page<WalletTransactionResponse>>> getMyTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = (Long) authentication.getPrincipal();
        Page<WalletTransactionResponse> transactions = walletService.getMyTransactions(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(transactions, "Transactions retrieved"));
    }

    @PostMapping("/pay-fee")
    public ResponseEntity<ApiResponse<?>> payPlatformFee(
            @Valid @RequestBody PayFeeRequest request,
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();

        if ("CREDIT_CARD".equalsIgnoreCase(request.getPaymentMethod())) {
            StripePaymentIntentResponse stripeResponse = walletService.payPlatformFeeCreditCard(providerId, request.getAmount());
            return ResponseEntity.ok(ApiResponse.success(stripeResponse, "Payment intent created. Complete payment in app."));
        } else {
            PaymentReceiptResponse receipt = walletService.payPlatformFeeCash(providerId, request.getAmount());
            return ResponseEntity.ok(ApiResponse.success(receipt, "Receipt generated. Pay at any supermarket."));
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<PaymentReceiptResponse>> requestWithdrawal(
            @Valid @RequestBody WithdrawRequest request,
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        PaymentReceiptResponse receipt = walletService.withdrawCash(providerId, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(receipt, "Receipt generated. Collect from any supermarket."));
    }
}
