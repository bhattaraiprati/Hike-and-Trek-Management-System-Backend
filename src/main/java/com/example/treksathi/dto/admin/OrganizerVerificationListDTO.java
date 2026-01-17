package com.example.treksathi.dto.admin;

import com.example.treksathi.enums.Approval_status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrganizerVerificationListDTO {
    private int id;

    // User details
    private int userId;
    private String email;
    private String fullName;
    private String profileImage;

    // Organizer details
    private String organizationName;
    private String contactPerson;
    private String address;
    private String phone;
    private String documentUrl;

    // Status
    private Approval_status approvalStatus;
    private String verifiedByName;
    private LocalDateTime verifiedOn;
    private LocalDateTime createdAt;

    // Stats
    private int eventsCount;
}
