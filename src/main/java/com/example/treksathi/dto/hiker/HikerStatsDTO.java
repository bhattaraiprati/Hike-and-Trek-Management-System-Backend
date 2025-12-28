package com.example.treksathi.dto.hiker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HikerStatsDTO {
    private Integer upcomingEvents;
    private Integer completedTrips;
    private Integer totalEvents;
    private Double totalDistance; // in km
    private Integer unreadNotifications;
}
