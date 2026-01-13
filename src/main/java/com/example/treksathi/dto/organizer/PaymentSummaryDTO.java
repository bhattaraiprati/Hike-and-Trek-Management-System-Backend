package com.example.treksathi.dto.organizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryDTO {
    private Double totalIncome;
    private Integer completedPayments;
    private Integer pendingPayments;
    private Integer refundedPayments;
    private Double monthlyGrowth; // percentage
    private String currency;
    private Integer totalParticipants;
    private Double averagePayment;
    private Integer platformFee; // percentage
}

