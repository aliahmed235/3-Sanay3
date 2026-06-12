package com.sany3.graduation_project.Services;

import com.stripe.model.PaymentIntent;
import com.sany3.graduation_project.Repositories.PaymentRepository;
import com.sany3.graduation_project.Repositories.ServiceOfferRepository;
import com.sany3.graduation_project.Repositories.ServiceRequestRepository;
import com.sany3.graduation_project.dto.response.PaymentResponse;
import com.sany3.graduation_project.entites.*;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.15");

    private final PaymentRepository paymentRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceOfferRepository serviceOfferRepository;
    private final WalletService walletService;
    private final StripeService stripeService;

    public PaymentResponse processCashPayment(Long customerId, Long requestId) {
        log.info("Processing cash payment: customer {}, request {}", customerId, requestId);

        ServiceRequest request = getCompletedRequest(requestId, customerId);
        checkAndCleanPayment(requestId);

        BigDecimal amount = getRequestAmount(request);
        BigDecimal platformFee = amount.multiply(PLATFORM_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal providerEarning = amount.subtract(platformFee);

        Payment payment = Payment.builder()
                .serviceRequest(request)
                .customer(request.getCustomer())
                .provider(request.getAcceptedProvider())
                .amount(amount)
                .paymentMethod(PaymentMethod.CASH)
                .platformFee(platformFee)
                .providerEarning(providerEarning)
                .status(PaymentStatus.COMPLETED)
                .build();
        payment = paymentRepository.save(payment);

        walletService.processCashPayment(request.getAcceptedProvider(), request, amount);

        log.info("Cash payment completed: payment {}", payment.getId());
        return toResponse(payment);
    }

    public PaymentResponse processCardPayment(Long customerId, Long requestId) {
        log.info("Processing credit card payment: customer {}, request {}", customerId, requestId);

        ServiceRequest request = getCompletedRequest(requestId, customerId);
        checkAndCleanPayment(requestId);

        BigDecimal amount = getRequestAmount(request);
        BigDecimal platformFee = amount.multiply(PLATFORM_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal providerEarning = amount.subtract(platformFee);

        try {
            PaymentIntent intent = stripeService.createPaymentIntent(
                    amount, "egp", "Payment for: " + request.getTitle());

            Payment payment = Payment.builder()
                    .serviceRequest(request)
                    .customer(request.getCustomer())
                    .provider(request.getAcceptedProvider())
                    .amount(amount)
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .stripePaymentIntentId(intent.getId())
                    .platformFee(platformFee)
                    .providerEarning(providerEarning)
                    .status(PaymentStatus.PENDING)
                    .build();
            payment = paymentRepository.save(payment);

            log.info("Credit card payment PENDING: payment {}, intent {}", payment.getId(), intent.getId());
            PaymentResponse response = toResponse(payment);
            response.setStripeClientSecret(intent.getClientSecret());
            response.setStripePaymentIntentId(intent.getId());
            return response;
        } catch (Exception e) {
            log.error("Stripe error: {}", e.getMessage());
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }

    public PaymentResponse confirmCardPayment(Long customerId, String paymentIntentId) {
        log.info("Confirming credit card payment: intent {}", paymentIntentId);

        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for intent: " + paymentIntentId));

        if (!payment.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("You can only confirm your own payments");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return toResponse(payment);
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        walletService.processCreditCardPayment(
                payment.getProvider(),
                payment.getServiceRequest(),
                payment.getAmount());

        log.info("Credit card payment COMPLETED: payment {}", payment.getId());
        return toResponse(payment);
    }

    private void checkAndCleanPayment(Long requestId) {
        if (paymentRepository.existsByServiceRequestIdAndStatus(requestId, PaymentStatus.COMPLETED)) {
            throw new IllegalStateException("Payment already completed for this request");
        }
        paymentRepository.deleteByServiceRequestIdAndStatusNot(requestId, PaymentStatus.COMPLETED);
    }

    private ServiceRequest getCompletedRequest(Long requestId, Long customerId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!request.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("You can only pay for your own requests");
        }

        if (request.getStatus() != RequestStatus.COMPLETED) {
            throw new IllegalStateException("Can only pay for completed requests");
        }

        if (request.getAcceptedProvider() == null) {
            throw new IllegalStateException("Request has no accepted provider");
        }

        return request;
    }

    private BigDecimal getRequestAmount(ServiceRequest request) {
        return serviceOfferRepository.findAcceptedOfferPrice(request.getId())
                .orElseThrow(() -> new IllegalStateException("Could not determine request amount - no accepted offer found"));
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .serviceRequestId(payment.getServiceRequest().getId())
                .serviceRequestTitle(payment.getServiceRequest().getTitle())
                .customerId(payment.getCustomer().getId())
                .customerName(payment.getCustomer().getName())
                .providerId(payment.getProvider().getId())
                .providerName(payment.getProvider().getName())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .platformFee(payment.getPlatformFee())
                .providerEarning(payment.getProviderEarning())
                .status(payment.getStatus().name())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
