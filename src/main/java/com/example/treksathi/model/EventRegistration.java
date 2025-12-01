package com.example.treksathi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.w3c.dom.stylesheets.LinkStyle;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    private LocalDateTime registrationDate;

    private String contact;

    private String contactName;
    private String email;

    private String paymentStatus;


    @OneToOne(mappedBy = "eventRegistration", fetch = FetchType.LAZY)
    private Payments payments;

    @OneToMany(mappedBy = "eventRegistration", fetch = FetchType.EAGER)
    private List<EventParticipants> eventParticipants;

}
