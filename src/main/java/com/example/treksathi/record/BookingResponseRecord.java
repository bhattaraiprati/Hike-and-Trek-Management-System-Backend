package com.example.treksathi.record;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record BookingResponseRecord(

        // Booking Info
        int bookingId,
        LocalDateTime bookingDate,

        EventDetails event,

        List<ParticipantDetails> participants,

        PaymentDetails payment
) {

    public record EventDetails(
            int id,
            String title,
            String location,
            LocalDate date,
            int durationDays,
            String status,
            String difficultyLevel,
            double price,
            String bannerImageUrl,
            String meetingPoint,
            LocalTime meetingTime,
            BookingResponseRecord.OrganizerInfo organizer
    ) {}

    public record OrganizerInfo(
            String name
    ) {}

    public record ParticipantDetails(
            int count
    ) {}

    public record PaymentDetails(
            String method,
            double amount,
            String status
    ) {}
}
