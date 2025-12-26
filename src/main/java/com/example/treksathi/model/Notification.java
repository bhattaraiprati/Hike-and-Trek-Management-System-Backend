package com.example.treksathi.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.treksathi.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Optional: Reference to related entity (e.g., event ID)
    private Integer referenceId;

    private String referenceType; // e.g., "EVENT", "TREK", "BOOKING"

    // Relationship to track recipients and their read status
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationRecipient> recipients = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Helper method to add recipients
    public void addRecipient(User user) {
        NotificationRecipient recipient = new NotificationRecipient();
        recipient.setNotification(this);
        recipient.setUser(user);
        recipient.setRead(false);
        recipients.add(recipient);
    }

    public void addRecipients(List<User> users) {
        users.forEach(this::addRecipient);
    }
}