package com.example.treksathi.dto.ChatBot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCardDTO {
    private Integer id;
    private String title;
    private String location;
    private String date;
    private Integer durationDays;
    private Double price;
    private String difficulty;
    private String organizer;
    private String bannerImageUrl;
    private Integer maxParticipants;
    private String status;
}
