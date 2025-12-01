package com.example.treksathi.record;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EventResponseRecord(
         int id,
        String title,
        String description,
        String location,
        LocalDate date,
        int durationDays,
        String difficultyLevel,
        double price,
        int maxParticipants,
        String meetingPoint,
        LocalTime meetingTime,
        String contactPerson,
        String contactEmail,
        String bannerImageUrl,
        List<String> includedServices,
        List<String> requirements,
        String status,
        OrganizerRecord organizer
) {
}
