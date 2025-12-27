package com.example.treksathi.dto.hiker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Main Dashboard Response
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HikerDashboardDTO {
    private UserInfoDTO userInfo;
    private HikerStatsDTO stats;
    private List<UpcomingAdventureDTO> upcomingAdventures;
    private List<RecommendedEventDTO> recommendedEvents;
    private List<RecentActivityDTO> recentActivities;
    private List<QuickActionDTO> quickActions;
}
