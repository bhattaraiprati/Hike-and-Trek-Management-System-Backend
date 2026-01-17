package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.IAdminOrganizerService;
import com.example.treksathi.config.JWTService;
import com.example.treksathi.dto.admin.AdminOrganizerStatsDTO;
import com.example.treksathi.dto.admin.OrganizerRejectionDTO;
import com.example.treksathi.dto.admin.OrganizerVerificationDetailDTO;
import com.example.treksathi.dto.admin.OrganizerVerificationListDTO;
import com.example.treksathi.enums.Approval_status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/organizers")
@Tag(name = "Admin Organizer APIs", description = "APIs for admin to manage organizer verification")
public class AdminOrganizerController {

    private final IAdminOrganizerService adminOrganizerService;
    private final JWTService jwtService;

    @GetMapping
    @Operation(summary = "Get all organizers", description = "Get list of organizers with optional status filter and search")
    public ResponseEntity<List<OrganizerVerificationListDTO>> getAllOrganizers(
            @RequestParam(required = false) Approval_status status,
            @RequestParam(required = false) String search) {
        List<OrganizerVerificationListDTO> organizers = adminOrganizerService.getAllOrganizers(status, search);
        return ResponseEntity.ok(organizers);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get verification statistics", description = "Get counts of organizers by status")
    public ResponseEntity<AdminOrganizerStatsDTO> getStats() {
        AdminOrganizerStatsDTO stats = adminOrganizerService.getStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organizer details", description = "Get detailed information of a specific organizer")
    public ResponseEntity<OrganizerVerificationDetailDTO> getOrganizerDetails(@PathVariable int id) {
        OrganizerVerificationDetailDTO organizer = adminOrganizerService.getOrganizerDetails(id);
        return ResponseEntity.ok(organizer);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve organizer", description = "Approve an organizer application")
    public ResponseEntity<OrganizerVerificationListDTO> approveOrganizer(
            @PathVariable int id,
            HttpServletRequest request) {
        int adminUserId = getAdminUserIdFromRequest(request);
        OrganizerVerificationListDTO organizer = adminOrganizerService.approveOrganizer(id, adminUserId);
        return ResponseEntity.ok(organizer);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject organizer", description = "Reject an organizer application with reason")
    public ResponseEntity<OrganizerVerificationListDTO> rejectOrganizer(
            @PathVariable int id,
            @Valid @RequestBody OrganizerRejectionDTO rejectionDTO,
            HttpServletRequest request) {
        int adminUserId = getAdminUserIdFromRequest(request);
        OrganizerVerificationListDTO organizer = adminOrganizerService.rejectOrganizer(id, adminUserId,
                rejectionDTO.getReason());
        return ResponseEntity.ok(organizer);
    }

    private int getAdminUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.getUserIdFromToken(token);
        }
        throw new RuntimeException("Unable to extract user ID from token");
    }
}
