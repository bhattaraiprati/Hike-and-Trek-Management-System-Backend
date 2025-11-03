package com.example.treksathi.dto.organizer;

import com.example.treksathi.enums.Approval_status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrganizerCreateDTO {
    @NotBlank(message = "Organization name is required")
    private String organizationName;

    @NotBlank(message = "Contact person is required")
    private String contactPerson;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Document URL is required")
    private String documentUrl;

    private Approval_status approvalStatus = Approval_status.PENDING;
}
