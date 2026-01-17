package com.example.treksathi.dto.event;

import com.example.treksathi.enums.DifficultyLevel;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdminEventDTO {
    private int id;
    private String title;
    private String description;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private double price;
    private int maxParticipants;
    private int currentParticipants;
    private String status; // Mapped to frontend status (PUBLISHED, DRAFT, etc.)
    private String imageUrl;
    private String category;
    private DifficultyLevel difficultyLevel;

    private EventOrganizerDTO organizer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
