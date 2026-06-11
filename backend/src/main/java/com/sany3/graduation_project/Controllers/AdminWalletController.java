package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Repositories.WalletRepository;
import com.sany3.graduation_project.Services.WalletService;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.ProviderDebtResponse;
import com.sany3.graduation_project.dto.response.WalletResponse;
import com.sany3.graduation_project.entites.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/wallets")
@RequiredArgsConstructor
@Slf4j
public class AdminWalletController {

    private final WalletRepository walletRepository;
    private final WalletService walletService;

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<ProviderDebtResponse>>> getAllProviderWallets() {
        List<Wallet> wallets = walletRepository.findAll();

        List<ProviderDebtResponse> providers = wallets.stream()
                .map(w -> ProviderDebtResponse.builder()
                        .providerId(w.getUser().getId())
                        .providerName(w.getUser().getName())
                        .providerEmail(w.getUser().getEmail())
                        .providerPhone(w.getUser().getPhone())
                        .balance(w.getBalance())
                        .banned(w.getUser().getBanned())
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.success(providers, "Provider wallets retrieved"));
    }

    @GetMapping("/providers/debtors")
    public ResponseEntity<ApiResponse<List<ProviderDebtResponse>>> getDebtors() {
        List<Wallet> wallets = walletRepository.findAll();

        List<ProviderDebtResponse> debtors = wallets.stream()
                .filter(w -> w.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .map(w -> ProviderDebtResponse.builder()
                        .providerId(w.getUser().getId())
                        .providerName(w.getUser().getName())
                        .providerEmail(w.getUser().getEmail())
                        .providerPhone(w.getUser().getPhone())
                        .balance(w.getBalance())
                        .banned(w.getUser().getBanned())
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.success(debtors, "Debtors retrieved"));
    }

    @PostMapping("/providers/{providerId}/payout")
    public ResponseEntity<ApiResponse<WalletResponse>> processPayout(
            @PathVariable Long providerId,
            @RequestBody Map<String, Object> body) {

        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String note = body.get("note") != null ? body.get("note").toString() : null;

        walletService.processPayout(providerId, amount, note);
        WalletResponse response = walletService.getMyWallet(providerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payout processed"));
    }
}
