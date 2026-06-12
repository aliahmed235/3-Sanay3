package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.PaymentService;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/cash")
    public ResponseEntity<ApiResponse<PaymentResponse>> payCash(
            @RequestBody Map<String, Long> body,
            Authentication authentication) {

        Long customerId = (Long) authentication.getPrincipal();
        Long requestId = body.get("requestId");

        PaymentResponse response = paymentService.processCashPayment(customerId, requestId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cash payment recorded"));
    }

    @PostMapping("/credit-card")
    public ResponseEntity<ApiResponse<PaymentResponse>> payCard(
            @RequestBody Map<String, Long> body,
            Authentication authentication) {

        Long customerId = (Long) authentication.getPrincipal();
        Long requestId = body.get("requestId");

        PaymentResponse response = paymentService.processCardPayment(customerId, requestId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Credit card payment completed"));
    }
}
