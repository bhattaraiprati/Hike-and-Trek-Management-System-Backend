package com.example.treksathi.dto.search;

import com.example.treksathi.enums.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    // General search
    private String query; // Search in title, description, location

    // Event filters
    private DifficultyLevel difficultyLevel;
    private Double minPrice;
    private Double maxPrice;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer minDuration;
    private Integer maxDuration;
    private String location;

    // Organizer filter
    private String organizerName;
    private Integer organizerId;

    // Pagination
    private Integer page = 0;
    private Integer size = 10;

    // Sorting
    private String sortBy = "date"; // date, price, title, popularity
    private String sortDirection = "ASC"; // ASC or DESC
}
