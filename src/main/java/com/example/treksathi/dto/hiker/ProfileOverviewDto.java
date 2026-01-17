package com.example.treksathi.dto.hiker;

import java.util.List;

public class ProfileOverviewDto {
    private Integer totalEvents;
    private Integer completedEvents;
    private Integer totalDistance; // in kilometers
    private List<RecentActivityDTO> recentActivityDTO;
}
