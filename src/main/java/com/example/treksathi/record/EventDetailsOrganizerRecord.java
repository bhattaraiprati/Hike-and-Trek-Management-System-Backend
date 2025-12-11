package com.example.treksathi.record;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record EventDetailsOrganizerRecord(
        int id,
        String title,
        String description,
        String location,
        LocalDate date,
        int durationDays,
        String difficultyLevel,
        double price,
        String bannerImageUrl,
        int maxParticipants,
        String meetingPoint,
        LocalTime meetingTime,
        String contactPerson,
        String contactEmail,
        List<String> includedServices,
        List<String> requirements,
        String status,
        LocalDateTime createdAt,

        List<EventRegistrationInfo> eventRegistration

) {
    public record EventRegistrationInfo(
            int id,
            LocalDateTime registrationDate,
            String contact,
            String contactName,
            String email,
            String status,
            PaymentInfo payments,
            List<ParticipantInfo> eventParticipants

    ){ }

    public record PaymentInfo(
            int id,
            Double amount,
            String method,
            String paymentStatus,
            LocalDateTime transactionDate

    ){}

    public record  ParticipantInfo(
            int id,
            String name,
            String gender,
            String nationality,
            String attendanceStatus
    ){

    }
}
