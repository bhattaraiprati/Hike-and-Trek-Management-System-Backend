package com.example.treksathi.model;

import com.example.treksathi.enums.ChatRoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
