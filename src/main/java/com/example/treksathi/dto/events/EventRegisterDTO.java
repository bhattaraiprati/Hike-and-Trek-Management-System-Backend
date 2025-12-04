package com.example.treksathi.dto.events;


import com.example.treksathi.enums.Gender;
import com.example.treksathi.enums.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class EventRegisterDTO {


    @NotNull(message = "Event ID is required")
    private Integer eventId;

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotBlank(message = "Contact is required")
    private String contact;

    @NotBlank(message = "Contact name is required")
    private String contactName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Participants list is required")
    private List<ParticipantDTO> participants;

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private String transactionUuid;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    @Data
    public static class ParticipantDTO {

        @NotBlank(message = "Participant name is required")
        private String name;

        @NotNull(message = "Gender is required")
        private Gender gender;

        @NotBlank(message = "Nationality is required")
        private String nationality;
    }
}
