package com.example.treksathi.dto.organizer;

import com.example.treksathi.enums.Approval_status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrganizerResponseDTO {

    private int id;

    private int userId;

    private String organizationName;
    private String contactPerson;
    private String address;
    private String documentUrl;

    private Approval_status approvalStatus;
    private String verifiedBy;
    private LocalDateTime verifiedOn;
}
