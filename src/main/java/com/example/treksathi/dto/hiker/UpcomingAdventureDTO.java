package com.example.treksathi.dto.hiker;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingAdventureDTO {
    private Integer id;
    private String title;
    private String location;
    private LocalDate date;
    private String difficulty;
    private String status; // CONFIRMED, PAYMENT_PENDING, WAITING_LIST
    private String imageUrl;
    private String organizer;
    private String meetingPoint;
    private Double price;
    private Integer daysUntil;
}
