package com.example.treksathi.dto.organizer;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PaymentFilterDTO {
    private DateRange dateRange;
    private String status; // ALL, COMPLETED, PENDING, FAILED, REFUNDED
    private Integer eventId;
    private String paymentMethod; // ALL, CREDIT_CARD, ESEWA, BANK_TRANSFER, CASH

    @Data
    public static class DateRange {
        private LocalDate from;
        private LocalDate to;
    }
}

