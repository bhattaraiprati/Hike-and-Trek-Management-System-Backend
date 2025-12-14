package com.example.treksathi.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;  // Use long for high volume

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // NoSQL-like: JSONB for flexible content (text, multimedia)
    @Column(columnDefinition = "jsonb")
    private String content;  // e.g., {"type": "text", "text": "Hello"} or {"type": "image", "url": "...", "metadata": {}}

    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<ReadReceipt> readReceipts = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
