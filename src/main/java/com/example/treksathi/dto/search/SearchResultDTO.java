package com.example.treksathi.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {
    private Integer id;
    private String title;
    private String description;
    private String location;
    private LocalDate date;
    private Integer durationDays;
    private String difficultyLevel;
    private Double price;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String bannerImageUrl;
    private String status;
    private OrganizerSearchDTO organizer;
    private Double averageRating;
    private Integer reviewCount;
}
