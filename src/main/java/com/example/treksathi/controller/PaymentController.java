package com.example.treksathi.controller;


import com.example.treksathi.Interfaces.IPaymentGatewayService;
import com.example.treksathi.Interfaces.IStripePaymentService;
import com.example.treksathi.dto.events.EventRegisterDTO;
import com.example.treksathi.dto.payments.StripePaymentResponse;
import com.example.treksathi.model.EventRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/hiker/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final IPaymentGatewayService paymentGatewayService;
    private final IStripePaymentService stripePaymentService;

    @PostMapping("/stripe/create-checkout-session")
    public ResponseEntity<?> createStripeCheckoutSession(@RequestBody EventRegisterDTO eventRegisterDTO) {
        try {
            log.info("Creating Stripe checkout session for user: {}", eventRegisterDTO.getUserId());
            StripePaymentResponse response = paymentGatewayService.initiateStripePayment(eventRegisterDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Stripe checkout session creation failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stripe/verify")
    public ResponseEntity<?> verifyStripePayment(@RequestParam("session_id") String sessionId) {
        try {
            log.info("Verifying Stripe payment for session: {}", sessionId);
            EventRegistration registration = stripePaymentService.handleSuccessfulPayment(sessionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "registrationId", registration.getId(),
                    "message", "Payment successful"
            ));
        } catch (Exception e) {
            log.error("Stripe payment verification failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }


}
