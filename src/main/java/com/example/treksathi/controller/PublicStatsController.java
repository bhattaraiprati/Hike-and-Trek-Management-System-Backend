package com.example.treksathi.controller;

import com.example.treksathi.dto.PlatformStatsDTO;
import com.example.treksathi.service.PublicStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/stats")
@RequiredArgsConstructor
@Tag(name = "Public Stats", description = "Endpoints for public platform statistics")
public class PublicStatsController {

    private final PublicStatsService publicStatsService;

    @GetMapping
    @Operation(summary = "Get platform-wide statistics", description = "Returns counts for trails, community members, and verified organizers for the About page.")
    public ResponseEntity<PlatformStatsDTO> getPlatformStats() {
        return ResponseEntity.ok(publicStatsService.getPlatformStats());
    }
}
