package com.example.treksathi.controller;

import com.example.treksathi.dto.admin.dashboard.AdminDashboardDTO;
import com.example.treksathi.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Endpoints for platform-wide analytics and monitoring")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get platform-wide dashboard analytics", description = "Returns summary stats, revenue charts, user growth data, and recent activity.")
    public ResponseEntity<AdminDashboardDTO> getDashboardData() {
        return ResponseEntity.ok(adminDashboardService.getDashboardData());
    }
}
