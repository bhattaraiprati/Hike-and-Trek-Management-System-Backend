package com.example.treksathi.controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.treksathi.Interfaces.IOrganizerService;
import com.example.treksathi.dto.organizer.OrganizerDashboardDTO;
import com.example.treksathi.dto.organizer.OrganizerProfileDTO;
import com.example.treksathi.dto.organizer.OrganizerRegistrationDTO;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Organizer Apis", description = "APIs related to Organizer Registration and Management")
public class OrganizerController {

    private final IOrganizerService organizerService;

    @GetMapping("/greet")
    public String greet(HttpServletRequest request){
        return "Hello from trek sathi";
    }

    @PostMapping("/organizer_register")
    public ResponseEntity<String> organizerRegister(@Valid @RequestBody OrganizerRegistrationDTO dto){
        try {
            organizerService.registerOrganizer(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("User Created Successfully");
        }
        catch (DuplicateKeyException e){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }

    }

    @GetMapping("/organizer/profile/{userId}")
    public ResponseEntity<OrganizerProfileDTO> getOrganizerProfile(@PathVariable int userId) {
        OrganizerProfileDTO profile = organizerService.getOrganizerProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/organizer/profile/{userId}")
    public ResponseEntity<OrganizerProfileDTO> updateOrganizerProfile(
            @PathVariable int userId,
            @RequestBody OrganizerProfileDTO profileDTO
    ) {
        OrganizerProfileDTO updatedProfile = organizerService.updateOrganizerProfile(userId, profileDTO);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/organizer/dashboard/{userId}")
    public ResponseEntity<OrganizerDashboardDTO> getOrganizerDashboard(@PathVariable int userId) {
        OrganizerDashboardDTO dashboard = organizerService.getOrganizerDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }
}
