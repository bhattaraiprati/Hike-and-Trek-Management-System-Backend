package com.example.treksathi.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrganizerRejectionDTO {
    @NotBlank(message = "Rejection reason is required")
    private String reason;
}
