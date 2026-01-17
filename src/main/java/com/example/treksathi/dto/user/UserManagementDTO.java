package com.example.treksathi.dto.user;

import com.example.treksathi.enums.AccountStatus;
import com.example.treksathi.enums.AuthProvidertype;
import com.example.treksathi.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserManagementDTO {
    private int id;
    private String email;
    private String name;
    private String phone;
    private String profileImage;
    private String providerId;
    private AuthProvidertype providerType;
    private Role role;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Organizer specific info (optional)
    private OrganizerInfoDTO organizer;

    // Stats
    private long totalBookings;
    private double totalSpent;
    private int reviewsCount;

    @Data
    @Builder
    public static class OrganizerInfoDTO {
        private int id;
        private String organizationName;
        private String approvalStatus;
    }
}
