package com.example.treksathi.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizerBalanceDTO {
    private int organizerId;
    private String organizerName;
    private String organization;
    private double pendingAmount;
    private double releasedAmount;
    private double totalBalance;
    private long pendingPayments;
}
