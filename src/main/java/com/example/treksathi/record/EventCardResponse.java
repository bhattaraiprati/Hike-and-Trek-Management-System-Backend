package com.example.treksathi.record;

import java.time.LocalDate;


public record EventCardResponse(
        int id,
        String title,
        String description,
        String location,
        LocalDate date,
        int durationDays,
        String difficultyLevel,
        double price,
        int maxParticipants,
        String bannerImageUrl,
        String status,
        long participantCount  // Changed from List to simple count
) {}
