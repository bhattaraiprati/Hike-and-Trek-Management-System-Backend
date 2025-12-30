package com.example.treksathi.dto.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripePaymentResponse {
    private String sessionId;
    private String sessionUrl;
    private String status;
    private Integer registrationId;
    private String message;
}
