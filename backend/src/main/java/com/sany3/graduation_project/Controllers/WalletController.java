package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.WalletService;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.WalletResponse;
import com.sany3.graduation_project.dto.response.WalletTransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

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
    public ResponseEntity<ApiResponse<WalletResponse>> payPlatformFee(
            @RequestBody Map<String, BigDecimal> body,
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        BigDecimal amount = body.get("amount");

        walletService.payPlatformFee(providerId, amount);
        WalletResponse response = walletService.getMyWallet(providerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Platform fee paid successfully"));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<WalletResponse>> requestWithdrawal(
            @RequestBody Map<String, BigDecimal> body,
            Authentication authentication) {

        Long providerId = (Long) authentication.getPrincipal();
        BigDecimal amount = body.get("amount");

        walletService.processWithdrawal(providerId, amount);
        WalletResponse response = walletService.getMyWallet(providerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Withdrawal request submitted"));
    }
}
