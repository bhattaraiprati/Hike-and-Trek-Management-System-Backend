package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.hiker.*;

import java.util.List;

public interface IHikerService {

    HikerDashboardDTO getDashboardData(String email);
    HikerStatsDTO getStats(String email);
    List<UpcomingAdventureDTO> getUpcomingAdventures(String email);
    List<RecommendedEventDTO> getRecommendedEvents(String email);
    List<RecentActivityDTO> getRecentActivity(String email);

}
