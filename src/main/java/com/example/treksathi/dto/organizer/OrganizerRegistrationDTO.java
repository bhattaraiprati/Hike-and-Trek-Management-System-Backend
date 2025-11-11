package com.example.treksathi.dto.organizer;

import com.example.treksathi.enums.Approval_status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrganizerRegistrationDTO {

    // From User
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Invalid phone number format")
    private String phone;

    // From Organizer
    @NotBlank(message = "Organization name is required")
    private String organizationName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "About section is required")
    @Size(max = 250, message = "About section cannot exceed 250 characters")
    private String about;

    @NotBlank(message = "Document URL is required")
    private String documentUrl;
}
