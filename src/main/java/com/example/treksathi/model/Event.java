package com.example.treksathi.model;

import com.example.treksathi.enums.DifficultyLevel;
import com.example.treksathi.enums.EventStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    private LocalDate date;
    private int durationDays;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private String category;

    private double price;
    private int maxParticipants;

    private String meetingPoint;
    private LocalTime meetingTime;

    private String contactPerson;
    private String contactEmail;

    private String bannerImageUrl;

    @ElementCollection
    private List<String> includedServices;

    @ElementCollection
    private List<String> requirements;

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.ACTIVE;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL)
    private ChatRoom chatRoom;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EventRegistration> eventRegistration;

    @OneToMany(mappedBy = "events", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Reviews> reviews;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
