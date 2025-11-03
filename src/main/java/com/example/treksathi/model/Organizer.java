package com.example.treksathi.model;

import com.example.treksathi.enums.Approval_status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Organizer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private String organization_name;
    private String contact_person;
    private String address;
    private String document_url;
    private Approval_status approval_status;
    private String verified_by;
    private LocalDateTime verified_on;
}
