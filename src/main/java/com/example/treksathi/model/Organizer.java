package com.example.treksathi.model;

import com.example.treksathi.enums.Approval_status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private String phone;
    private String cover_image;
    private String document_url;
    @Column(length = 250)
    private String about;
    @Enumerated(EnumType.STRING)
    private Approval_status approvalStatus = Approval_status.PENDING;

    @OneToMany(mappedBy = "organizer")
    private List<Event> events;
    @ManyToOne
    @JoinColumn(name = "verified_by")
    private User verified_by;
    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL)
    private List<ChatRoom> chatRooms = new ArrayList<>();
    private LocalDateTime verified_on;
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
