package com.example.treksathi.model;


import com.example.treksathi.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class EventParticipants {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "event_registration_id", nullable = false)
    private EventRegistration eventRegistration;

    private String name;
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String nationality;

    private String attendanceStatus;
}
