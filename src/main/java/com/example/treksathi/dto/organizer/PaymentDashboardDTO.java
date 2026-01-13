package com.example.treksathi.dto.organizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDashboardDTO {
    private PaymentSummaryDTO summary;
    private List<EventPaymentDTO> events;
    private List<ParticipantPaymentDTO> participantPayments;
    private List<ParticipantPaymentDTO> recentPayments;
    private List<MonthlyRevenueDTO> revenueChart;
}

