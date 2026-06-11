package com.sany3.graduation_project.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sany3.graduation_project.Services.PaymentService;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.PaymentResponse;
import com.sany3.graduation_project.dto.response.StripePaymentIntentResponse;
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
    private final ObjectMapper objectMapper;

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

    @PostMapping("/stripe/create-intent")
    public ResponseEntity<ApiResponse<StripePaymentIntentResponse>> createStripeIntent(
            @RequestBody Map<String, Long> body,
            Authentication authentication) {

        Long customerId = (Long) authentication.getPrincipal();
        Long requestId = body.get("requestId");

        StripePaymentIntentResponse response = paymentService.createStripePaymentIntent(customerId, requestId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment intent created"));
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> stripeWebhook(@RequestBody String payload) {
        log.info("Stripe webhook received");

        try {
            JsonNode root = objectMapper.readTree(payload);
            String type = root.get("type").asText();

            if ("payment_intent.succeeded".equals(type)) {
                String paymentIntentId = root.get("data")
                        .get("object")
                        .get("id").asText();

                paymentService.handleStripeWebhook(paymentIntentId);
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook error");
        }
    }
}
