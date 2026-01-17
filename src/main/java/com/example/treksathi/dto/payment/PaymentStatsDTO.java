package com.example.treksathi.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentStatsDTO {
    private double totalRevenue;
    private long pendingPayments;
    private long completedPayments;
    private long releasedPayments;
    private double totalFee;
    private double netRevenue;
    private double todayRevenue;
    private double averagePayment;
}
