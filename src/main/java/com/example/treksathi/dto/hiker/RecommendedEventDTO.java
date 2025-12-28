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
public class RecommendedEventDTO {
    private Integer id;
    private String title;
    private String location;
    private LocalDate startDate;
    private String difficulty;
    private String imageUrl;
    private Double rating;
    private Integer totalRatings;
    private Integer participants;
    private Integer maxParticipants;
    private Double price;
    private String duration;
    private Integer matchPercentage; // Based on user preferences
}
