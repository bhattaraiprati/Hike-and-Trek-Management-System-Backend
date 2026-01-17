package com.example.treksathi.controller;

import com.example.treksathi.dto.event.AdminEventDTO;
import com.example.treksathi.dto.event.EventParticipantDTO;
import com.example.treksathi.dto.event.EventStatsDTO;
import com.example.treksathi.service.AdminEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final AdminEventService adminEventService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get all events with filtering", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<AdminEventDTO>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminEventService.getAllEvents(page, size, status, search));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get event statistics", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<EventStatsDTO> getEventStats() {
        return ResponseEntity.ok(adminEventService.getEventStats());
    }

    @GetMapping("/{id}/participants")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get event participants", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<EventParticipantDTO>> getEventParticipants(@PathVariable int id) {
        return ResponseEntity.ok(adminEventService.getEventParticipants(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update event status", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AdminEventDTO> updateEventStatus(
            @PathVariable int id,
            @RequestParam String status) {
        return ResponseEntity.ok(adminEventService.updateEventStatus(id, status));
    }
}
