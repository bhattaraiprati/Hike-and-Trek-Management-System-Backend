package com.example.treksathi.dto.organizer;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class EventResponseDTO {

    private int id;
    private String title;
    private String description;
    private String location;

    private LocalDate date;
    private int durationDays;
    private String difficultyLevel;

    private double price;
    private int maxParticipants;

    private String meetingPoint;
    private LocalTime meetingTime;

    private String contactPerson;
    private String contactEmail;

    private String bannerImageUrl;

    private List<String> includedServices;
    private List<String> requirements;

    private String status;
}
