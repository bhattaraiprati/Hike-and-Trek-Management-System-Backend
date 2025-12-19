package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.IOrganizerService;
import com.example.treksathi.dto.organizer.OrganizerRegistrationDTO;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.service.OrganizerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
