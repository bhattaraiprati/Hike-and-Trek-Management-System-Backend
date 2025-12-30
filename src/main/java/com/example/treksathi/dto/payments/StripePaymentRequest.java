package com.example.treksathi.dto.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripePaymentRequest {
    private Long amount; // Amount in cents
    private String currency;
    private String successUrl;
    private String cancelUrl;
    private String customerEmail;
    private String description;
    private Integer registrationId;
}
