package com.example.treksathi.dto.favourites;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteEventDTO {
    private Integer favouriteId;
    private Integer eventId;
    private String title;
    private String description;
    private String location;
    private LocalDate date;
    private String difficulty;
    private Double price;
    private String imageUrl;
    private String organizerName;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Double rating;
    private Integer totalRatings;
    private LocalDateTime addedAt;
    private Boolean isAvailable; // Event still active and in future
    private Boolean isRegistered; // User already registered
}
