package com.example.treksathi.dto.hiker;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDTO {
    private Integer id;
    private String type; // REGISTRATION, PAYMENT, MESSAGE, REVIEW, BOOKING
    private String title;
    private String description;
    private LocalDateTime timestamp;
    private Integer eventId;
    private Boolean isRead;
}
