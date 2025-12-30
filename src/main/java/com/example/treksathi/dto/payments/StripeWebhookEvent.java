package com.example.treksathi.dto.payments;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeWebhookEvent {
    private String eventType;
    private String sessionId;
    private String paymentIntentId;
    private String paymentStatus;
    private Long amountTotal;
    private String currency;
    private String customerEmail;
}
