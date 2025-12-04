package com.example.treksathi.record;

import java.time.LocalDateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EventRegistrationResponse(

        // Booking Info
        int bookingId,
        LocalDateTime bookingDate,
        String bookingStatus,
        double totalAmount,

        String contactName,
        String contactPhone,
        String contactEmail,

        EventDetails event,

        List<ParticipantDetails> participants,

        PaymentDetails payment
) {

    public record EventDetails(
            int id,
            String title,
            String description,
            String location,
            LocalDate date,
            int durationDays,
            String difficultyLevel,
            double price,
            String bannerImageUrl,
            String meetingPoint,
            LocalTime meetingTime,
            OrganizerInfo organizer
    ) {}

    public record OrganizerInfo(
            String name,
            String contactPerson,
            String contactEmail,
            String phone
    ) {}

    public record ParticipantDetails(
            String title,

            String fullName,
            String nationality,
            String gender
    ) {}

    public record PaymentDetails(
            String method,
            String transactionId,
            double amount,
            LocalDateTime paidAt,
            String status
    ) {}
}

