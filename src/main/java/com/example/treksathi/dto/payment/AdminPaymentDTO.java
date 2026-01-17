package com.example.treksathi.dto.payment;

import com.example.treksathi.enums.PaymentMethod;
import com.example.treksathi.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminPaymentDTO {
    private int id;
    private String transactionId;
    private double amount;
    private double fee;
    private double netAmount;
    private String currency; // Keeping it string as frontend expects "USD" or similar, backend might default
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;

    // Release info
    private LocalDateTime releasedDate; // or releasedAt
    private String releaseNotes;
    private UserInfo releasedBy;

    // Verification info
    private LocalDateTime verifiedAt;
    private UserInfo verifiedBy;

    private UserInfo user;
    private EventInfo event;
    private OrganizerInfo organizer;
    private RegistrationInfo registration;

    @Data
    @Builder
    public static class UserInfo {
        private int id;
        private String name;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    public static class EventInfo {
        private int id;
        private String title;
        private String date; // LocalDate to string
        private String location;
    }

    @Data
    @Builder
    public static class OrganizerInfo {
        private int id;
        private String name;
        private String organization; // organization_name
        private String email;
    }

    @Data
    @Builder
    public static class RegistrationInfo {
        private int id;
        private int participants;
        private LocalDateTime bookingDate;
    }
}
