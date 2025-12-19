package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.IEventService;
import com.example.treksathi.Interfaces.IPaymentGatewayService;
import com.example.treksathi.dto.events.EventRegisterDTO;
import com.example.treksathi.dto.pagination.PaginatedResponseDTO;
import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.record.EventCardResponse;
import com.example.treksathi.record.EventResponseRecord;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Event Apis", description = "APIs related to Event Listing, Details, and Registration with Payment Integration")
public class EventController {

    private final IEventService eventService;

    private final IPaymentGatewayService paymentGatewayService;

    private static final String FRONTEND_URL = "http://localhost:5173";

    // Get all event
    @GetMapping("/all")
    public ResponseEntity<PaginatedResponseDTO<EventCardResponse>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginatedResponseDTO<EventCardResponse> events = eventService.getAllEvents(page, size);
        return ResponseEntity.ok(events);
    }
    // Get Event details by the event ID
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseRecord> getEventById(@PathVariable int id) {
        EventResponseRecord event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/register/event")
    public ResponseEntity<?> registerEvent(@RequestBody EventRegisterDTO eventRegisterDTO){
    Object paymentRequest = paymentGatewayService.initiateEsewaPayment(eventRegisterDTO);
        return ResponseEntity.ok(paymentRequest);
    }


    @GetMapping("/registration/success")
    public ResponseEntity<Void> handleEsewaSuccess(@RequestParam(value = "data", required = false) String data) {
        log.info("eSewa success callback received");

        try {
            EventRegistration registration = paymentGatewayService.verifyAndConfirmPayment(data);
            String redirectUrl = String.format(
                    "%s/hiker-dashboard/booking-confirmation/%d?status=success",
                    FRONTEND_URL, registration.getId()
            );
            return ResponseEntity.status(302).header("Location", redirectUrl).build();

        } catch (Exception e) {
            log.error("Payment failed: {}", e.getMessage());
            return ResponseEntity.status(302)
                    .header("Location", FRONTEND_URL + "/hiker-dashboard/booking-confirmation?status=failed&error=payment_failed")
                    .build();
        }
    }

    @GetMapping("/registration/failure")
    public ResponseEntity<Void> handleEsewaFailure() {
        log.info("eSewa payment failed or cancelled");
        return ResponseEntity.status(302)
                .header("Location", FRONTEND_URL + "/hiker-dashboard/booking-confirmation?status=failed&error=cancelled")
                .build();
    }



    @GetMapping("/check")
    public ResponseEntity<?> checkTokenExpiry() {

        return ResponseEntity.ok("Successful");
    }

}
