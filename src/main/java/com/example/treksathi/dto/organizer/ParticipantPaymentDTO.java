package com.example.treksathi.dto.organizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantPaymentDTO {
    private Integer id;
    private String participantName;
    private String participantEmail;
    private Integer eventId;
    private String eventTitle;
    private Integer numberOfParticipants;
    private LocalDateTime paymentDate;
    private Double amount;
    private String status; // COMPLETED, PENDING, FAILED, REFUNDED
    private String paymentMethod; // CREDIT_CARD, ESEWA, BANK_TRANSFER, CASH
    private String transactionId;
    private String receiptUrl;
}

