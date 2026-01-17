package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.IEventRegistrationService;
import com.example.treksathi.dto.user.UserManagementDTO;
import com.example.treksathi.service.UserServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin User Management", description = "APIs for managing users (admin only)")
public class AdminUserController {

    private final UserServices userServices;
    private final IEventRegistrationService eventRegistrationService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get all users", description = "Get paginated list of users with filtering", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<UserManagementDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ALL") String role,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "") String search) {
        Page<UserManagementDTO> users = userServices.getAllUsers(page, size, role, status, search);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update user status", description = "Update user account status (ACTIVE, INACTIVE, SUSPENDED)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserManagementDTO> updateUserStatus(
            @PathVariable int id,
            @RequestParam String status) {
        UserManagementDTO updatedUser = userServices.updateUserStatus(id, status);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/bookingDetails/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get user booking details by ID", description = "Retrieve user booking details by user ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserManagementDTO> getUserById(@PathVariable int id) {
        UserManagementDTO user = userServices.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
