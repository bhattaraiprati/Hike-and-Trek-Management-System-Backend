package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.IHikerService;
import com.example.treksathi.dto.hiker.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hiker")
public class HikerController {

    private final IHikerService hikerService;

    @GetMapping("/dashboard")
    public ResponseEntity<HikerDashboardDTO> getDashboard(Authentication authentication) {
        String email = authentication.getName();
        HikerDashboardDTO dashboard = hikerService.getDashboardData(email);
        return ResponseEntity.ok(dashboard);
    }

    // Optional: Separate endpoints for lazy loading
    @GetMapping("/dashboard/stats")
    public ResponseEntity<HikerStatsDTO> getStats(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(hikerService.getStats(email));
    }

    @GetMapping("/dashboard/upcoming-events")
    public ResponseEntity<List<UpcomingAdventureDTO>> getUpcomingEvents(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(hikerService.getUpcomingAdventures(email));
    }

    @GetMapping("/dashboard/recommended")
    public ResponseEntity<List<RecommendedEventDTO>> getRecommended(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(hikerService.getRecommendedEvents(email));
    }

    @GetMapping("/dashboard/activity")
    public ResponseEntity<List<RecentActivityDTO>> getActivity(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(hikerService.getRecentActivity(email));
    }


}
