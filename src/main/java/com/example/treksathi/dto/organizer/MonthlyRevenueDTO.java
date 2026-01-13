package com.example.treksathi.dto.organizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueDTO {
    private String month; // e.g., "Jan 2024"
    private Double revenue;
    private Double growth; // percentage
    private Integer events;
    private Integer participants;
}

