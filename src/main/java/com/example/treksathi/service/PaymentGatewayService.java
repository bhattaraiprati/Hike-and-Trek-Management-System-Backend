package com.example.treksathi.service;

import com.example.treksathi.Interfaces.IPaymentGatewayService;
import com.example.treksathi.dto.events.EsewaStatusResponse;
import com.example.treksathi.dto.events.EventRegisterDTO;
import com.example.treksathi.enums.EventRegistrationStatus;
import com.example.treksathi.enums.PaymentStatus;
import com.example.treksathi.exception.EventNotFoundException;
import com.example.treksathi.exception.NotFoundException;
import com.example.treksathi.model.*;
import com.example.treksathi.record.EsewaPaymentRequest;
import com.example.treksathi.repository.*;
import com.example.treksathi.util.SignatureUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayService  implements IPaymentGatewayService {

    @Value("${ESEWA.GATEWAY.URL}")
    private String ESEWA_GATEWAY_URL;
    @Value("${ESEWA.PRODUCT.CODE}")
    private String ESEWA_PRODUCT_CODE;
    @Value("${ESEWA.SUCCESS.URL}")
    private String SUCCESS_URL;
    @Value("${ESEWA.FAILURE.URL}")
    private String FAILURE_URL;

    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentsRepository;
    private final EventParticipantsRepository eventParticipantsRepository;
    private final RestTemplate restTemplate;

    @Transactional
    public Object initiateEsewaPayment(EventRegisterDTO eventRegisterDTO) {
        String transactionUuid = UUID.randomUUID().toString();
        eventRegisterDTO.setTransactionUuid(transactionUuid);
        log.info("Initiating eSewa payment with transaction_uuid: {}", transactionUuid);
        log.info("Received totalAmount: {}", eventRegisterDTO.getAmount());

        EventRegistration eventRegistration = createEvent(eventRegisterDTO);
        String totalAmount = String.format("%.2f", eventRegisterDTO.getAmount());
        String signedFieldNames = "total_amount,transaction_uuid,product_code";
        String signature = SignatureUtil.generateSignature(totalAmount, transactionUuid, ESEWA_PRODUCT_CODE);

        EsewaPaymentRequest paymentRequest = new EsewaPaymentRequest(
                totalAmount,
                "0",
                totalAmount,
                transactionUuid,
                ESEWA_PRODUCT_CODE,
                "0",
                "0",
                SUCCESS_URL,
                FAILURE_URL,
                signedFieldNames,
                signature,
                eventRegistration.getId()
        );

        log.info("eSewa payment request payload: {}", paymentRequest);
        return paymentRequest;
    }

    @Transactional
    public EventRegistration createEvent(EventRegisterDTO eventRegisterDTO) {
        // Fetch Event and User entities
        Event event = eventRepository.findById(eventRegisterDTO.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventRegisterDTO.getEventId()));

        User user = userRepository.findById(eventRegisterDTO.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + eventRegisterDTO.getUserId()));

        // Create EventRegistration
        EventRegistration eventRegistration = new EventRegistration();
        eventRegistration.setEvent(event);
        eventRegistration.setUser(user);
        mapDtoToEntity(eventRegisterDTO, eventRegistration);
        eventRegistration.setStatus(EventRegistrationStatus.SUCCESS);

        // Save EventRegistration first to get the ID
        eventRegistration = eventRegistrationRepository.save(eventRegistration);
        log.info("Created EventRegistration with ID: {}", eventRegistration.getId());

        // Create and save Payments
        Payments payment = new Payments();
        payment.setEventRegistration(eventRegistration);
        payment.setAmount(eventRegisterDTO.getAmount());
        payment.setMethod(eventRegisterDTO.getMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionUuid(eventRegisterDTO.getTransactionUuid());
        payment.setTransactionDate(LocalDateTime.now());

        Payments savedPayment = paymentsRepository.save(payment);
        eventRegistration.setPayments(savedPayment); // ← ADD THIS LINE
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

    @Transactional
    public EventRegistration verifyAndConfirmPayment(String base64Data) throws Exception {
        if (base64Data == null || base64Data.isBlank()) {
            throw new IllegalArgumentException("Missing payment data from eSewa");
        }

        try {
            // 1. Decode Base64
            String decoded = new String(Base64.getDecoder().decode(base64Data));
            log.info("Decoded eSewa data: {}", decoded);

            Map<String, String> data = new ObjectMapper()
                    .readValue(decoded, Map.class);

            String transactionUuid = data.get("transaction_uuid");
            String totalAmount = data.get("total_amount");
            String transactionCode = data.get("transaction_code");
            String status = data.get("status");
            String productCode = data.get("product_code");
            String signedFieldNames = data.get("signed_field_names");
            String receivedSignature = data.get("signature");

            // 2. Validate required fields
            if (transactionUuid == null || totalAmount == null || transactionCode == null ||
                    status == null || productCode == null || receivedSignature == null) {
                throw new IllegalArgumentException("Invalid eSewa response: missing fields");
            }

            // 3. Verify Signature (Security Check)
            String expectedSignature = SignatureUtil.generateSignature(
                    transactionCode, status, totalAmount, transactionUuid, productCode, signedFieldNames
            );

            if (!receivedSignature.equals(expectedSignature)) {
                log.error("Signature mismatch! Received: {}, Expected: {}", receivedSignature, expectedSignature);
                throw new SecurityException("Invalid eSewa signature");
            }

            // 4. Verify with eSewa API
            boolean isVerified = verifyWithEsewaApi(transactionUuid, totalAmount, productCode);

            if (!isVerified || !"COMPLETE".equals(status)) {
                throw new RuntimeException("Payment not completed");
            }

            // 5. Update Database
            Payments payment = paymentsRepository.findByTransactionUuid(transactionUuid)
                    .orElseThrow(() -> new NotFoundException("Payment not found"));

            EventRegistration registration = payment.getEventRegistration();

            payment.setTransactionReference(transactionCode);
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            paymentsRepository.save(payment);

            registration.setStatus(EventRegistrationStatus.SUCCESS);
            eventRegistrationRepository.save(registration);

            log.info("Payment verified & booking confirmed for registration ID: {}", registration.getId());
            return registration;

        } catch (Exception e) {
            log.error("Payment verification failed: {}", e.getMessage(), e);
            throw new Exception("");
        }
    }

    private boolean verifyWithEsewaApi(String transactionUuid, String totalAmount, String productCode) {
        String url = String.format(
                "%s/api/epay/transaction/status/?product_code=%s&total_amount=%s&transaction_uuid=%s",
                ESEWA_GATEWAY_URL, productCode,
                String.format("%.2f", Double.parseDouble(totalAmount)),
                transactionUuid
        );

        try {
            EsewaStatusResponse response = restTemplate.getForObject(url, EsewaStatusResponse.class);
            log.info("eSewa API verification response: {}", response);

            return response != null && "COMPLETE".equals(response.getStatus());
        } catch (Exception e) {
            log.error("Failed to verify with eSewa API: {}", e.getMessage());
            return false;
        }
    }

    public EventRegistration getRegistrationByTransactionUuid(String uuid) {
        return paymentsRepository.findByTransactionUuid(uuid)
                .map(Payments::getEventRegistration)
                .orElseThrow(() -> new NotFoundException("Registration not found"));
    }

    private void mapDtoToEntity(EventRegisterDTO dto, EventRegistration event) {
        event.setEmail(dto.getEmail());
        event.setContact(dto.getContact());
        event.setContactName(dto.getContactName());
    }
}
