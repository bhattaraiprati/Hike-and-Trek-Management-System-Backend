package com.example.treksathi.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.treksathi.enums.ChatRoomType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;  // e.g., "Organizer X General" or "Event Y Discussion"

    @Enumerated(EnumType.STRING)
    private ChatRoomType type;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private Organizer organizer;  // Required for ORGANIZER_CHANNEL

    @OneToOne
    @JoinColumn(name = "event_id")
    private Event event;  // Required for EVENT_GROUP

    
    @ManyToMany
    @JoinTable(
            name = "chat_room_participants",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new ArrayList<>();

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> messages = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
