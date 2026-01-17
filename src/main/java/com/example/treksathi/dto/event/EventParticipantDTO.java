package com.example.treksathi.dto.event;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventParticipantDTO {
    private int id;
    private int userId;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime bookingDate;
    private String status; // CONFIRMED, CANCELLED, etc.
}
