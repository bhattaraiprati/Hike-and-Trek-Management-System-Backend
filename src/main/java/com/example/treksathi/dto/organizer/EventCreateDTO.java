package com.example.treksathi.dto.organizer;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class EventCreateDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Date is required")
    @Future(message = "Event date must be in the future")
    private LocalDate date;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Max(value = 365, message = "Duration cannot exceed 365 days")
    private int durationDays;

    @NotBlank(message = "Difficulty level is required")
    @Pattern(regexp = "EASY|MODERATE|DIFFICULT|EXTREME",
            message = "Difficulty level must be EASY, MODERATE, DIFFICULT, or EXTREME")
    private String difficultyLevel;

    @Min(value = 0, message = "Price cannot be negative")
    private double price;

    @Min(value = 1, message = "Maximum participants must be at least 1")
    @Max(value = 1000, message = "Maximum participants cannot exceed 1000")
    private int maxParticipants;

    @NotBlank(message = "Meeting point is required")
    private String meetingPoint;

    @NotNull(message = "Meeting time is required")
    private LocalTime meetingTime;

    @NotBlank(message = "Contact person is required")
    private String contactPerson;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;

    private String bannerImageUrl;

    private List<String> includedServices;
    private List<String> requirements;
}