package com.example.treksathi.model;

import com.example.treksathi.enums.DifficultyLevel;
import com.example.treksathi.enums.EventStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Organizer Link
    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    // Basic Info
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    private LocalDate date;
    private int durationDays;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private double price;
    private int maxParticipants;

    // Meeting info
    private String meetingPoint;
    private LocalTime meetingTime;

    // Contact
    private String contactPerson;
    private String contactEmail;

    // Media
    private String bannerImageUrl;

    // Lists
    @ElementCollection
    private List<String> includedServices;

    @ElementCollection
    private List<String> requirements;

    // System fields
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.PENDING;

    private String createdAt;
    private String updatedAt;
}
