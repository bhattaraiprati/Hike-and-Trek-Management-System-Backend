package com.example.treksathi.service;

import com.example.treksathi.Interfaces.IPaymentGatewayService;
import com.example.treksathi.Interfaces.IStripePaymentService;
import com.example.treksathi.dto.events.EventRegisterDTO;
import com.example.treksathi.dto.payments.StripePaymentResponse;
import com.example.treksathi.enums.EventRegistrationStatus;
import com.example.treksathi.enums.PaymentStatus;
import com.example.treksathi.exception.EventNotFoundException;
import com.example.treksathi.exception.NotFoundException;
import com.example.treksathi.model.*;
import com.example.treksathi.repository.*;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService implements IStripePaymentService {

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    // REMOVED: IPaymentGatewayService dependency to break circular reference
    // Instead, inject repositories directly
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final EventParticipantsRepository eventParticipantsRepository;

    @Override
    @Transactional
    public StripePaymentResponse createCheckoutSession(EventRegisterDTO eventRegisterDTO) throws Exception {
        try {
            // 1. Create event registration directly (instead of calling paymentGatewayService)
            EventRegistration eventRegistration = createEventRegistration(eventRegisterDTO);

            log.info("Creating Stripe checkout session for registration ID: {}", eventRegistration.getId());

            // 2. Convert amount to cents (Stripe uses smallest currency unit)
            long amountInCents = (long) (eventRegisterDTO.getAmount() * 100);

            // 3. Build event details for Stripe
            String eventTitle = eventRegistration.getEvent().getTitle();
            String eventDate = eventRegistration.getEvent().getDate().toString();
            int participantCount = eventRegisterDTO.getParticipants().size();

            // 4. Create Stripe Checkout Session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(eventTitle)
                                                                    .setDescription(
                                                                            String.format("Event Date: %s | Participants: %d",
                                                                                    eventDate, participantCount)
                                                                    )
                                                                    .build()
                                                    )
                                                    .setUnitAmount(amountInCents)
                                                    .build()
                                    )
                                    .setQuantity(1L)
                                    .build()
                    )
                    .setCustomerEmail(eventRegisterDTO.getEmail())
                    .putMetadata("registration_id", String.valueOf(eventRegistration.getId()))
                    .putMetadata("user_id", String.valueOf(eventRegisterDTO.getUserId()))
                    .putMetadata("event_id", String.valueOf(eventRegisterDTO.getEventId()))
                    .build();

            Session session = Session.create(params);

            // 5. Update payment record with session ID
            Payments payment = eventRegistration.getPayments();
            payment.setTransactionUuid(session.getId());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            log.info("Stripe session created successfully. Session ID: {}", session.getId());

            return StripePaymentResponse.builder()
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .status("pending")
                    .registrationId(eventRegistration.getId())
                    .message("Checkout session created successfully")
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error while creating checkout session: {}", e.getMessage(), e);
            throw new Exception("Failed to create Stripe checkout session: " + e.getMessage());
        }
    }

    // Helper method to create event registration (copied from PaymentGatewayService)

    private EventRegistration createEventRegistration(EventRegisterDTO eventRegisterDTO) {
        // Fetch Event and User entities
        Event event = eventRepository.findById(eventRegisterDTO.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventRegisterDTO.getEventId()));

        User user = userRepository.findById(eventRegisterDTO.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + eventRegisterDTO.getUserId()));

        // Create EventRegistration
        EventRegistration eventRegistration = new EventRegistration();
        eventRegistration.setEvent(event);
        eventRegistration.setUser(user);
        eventRegistration.setEmail(eventRegisterDTO.getEmail());
        eventRegistration.setContact(eventRegisterDTO.getContact());
        eventRegistration.setContactName(eventRegisterDTO.getContactName());
        eventRegistration.setStatus(EventRegistrationStatus.PENDING); // Set to PENDING initially

        // Save EventRegistration first to get the ID
        eventRegistration = eventRegistrationRepository.save(eventRegistration);
        log.info("Created EventRegistration with ID: {}", eventRegistration.getId());

        // Create and save Payments
        Payments payment = new Payments();
        payment.setEventRegistration(eventRegistration);
        payment.setAmount(eventRegisterDTO.getAmount());
        payment.setMethod(eventRegisterDTO.getMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionDate(LocalDateTime.now());

        Payments savedPayment = paymentRepository.save(payment);
        eventRegistration.setPayments(savedPayment);
        log.info("Created Payment record for EventRegistration ID: {}", eventRegistration.getId());

        // Create and save EventParticipants
        List<EventParticipants> participants = new ArrayList<>();
        for (EventRegisterDTO.ParticipantDTO participantDTO : eventRegisterDTO.getParticipants()) {
            EventParticipants participant = new EventParticipants();
            participant.setEventRegistration(eventRegistration);
            participant.setName(participantDTO.getName());
            participant.setGender(participantDTO.getGender());
            participant.setNationality(participantDTO.getNationality());
            participant.setAttendanceStatus("REGISTERED");
            participants.add(participant);
        }
        eventParticipantsRepository.saveAll(participants);
        log.info("Created {} participants for EventRegistration ID: {}", participants.size(), eventRegistration.getId());

        eventRegistration.setEventParticipants(participants);
        return eventRegistration;
    }

    @Override
    @Transactional
    public EventRegistration handleSuccessfulPayment(String sessionId) throws Exception {
        try {
            log.info("Verifying payment for session: {}", sessionId);

            // 1. Retrieve session from Stripe to verify
            Session session = Session.retrieve(sessionId);

            log.info("Session status: {}, Payment status: {}", session.getStatus(), session.getPaymentStatus());

            // 2. Verify payment was completed
            if (!"complete".equals(session.getStatus())) {
                throw new Exception("Session not completed. Status: " + session.getStatus());
            }

            if (!"paid".equals(session.getPaymentStatus())) {
                throw new Exception("Payment not completed. Payment Status: " + session.getPaymentStatus());
            }

            // 3. Find payment record
            Payments payment = paymentRepository.findByTransactionUuid(sessionId)
                    .orElseThrow(() -> new NotFoundException("Payment record not found for session: " + sessionId));

            // 4. Check if already processed (prevent double processing)
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                log.info("Payment already processed for session: {}", sessionId);
                return payment.getEventRegistration();
            }

            // 5. Update payment status
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setTransactionReference(session.getPaymentIntent());
            paymentRepository.save(payment);

            // 6. Update registration status
            EventRegistration registration = payment.getEventRegistration();
            registration.setStatus(EventRegistrationStatus.SUCCESS);
            eventRegistrationRepository.save(registration);

            log.info("Payment confirmed successfully for registration ID: {}", registration.getId());
            return registration;

        } catch (StripeException e) {
            log.error("Stripe error while verifying payment: {}", e.getMessage(), e);
            throw new Exception("Failed to verify payment with Stripe: " + e.getMessage());
        }
    }
}
