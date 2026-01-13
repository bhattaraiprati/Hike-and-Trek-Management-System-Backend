package com.example.treksathi.dto.organizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPaymentDTO {
    private Integer id;
    private Integer eventId;
    private String eventTitle;
    private LocalDate eventDate;
    private Integer totalParticipants;
    private Integer paidParticipants;
    private Double totalRevenue;
    private Double averagePaymentPerPerson;
    private Double organizerShare;
    private String status; // ACTIVE, COMPLETED, CANCELLED
}

