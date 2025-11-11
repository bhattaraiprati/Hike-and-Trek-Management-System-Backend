package com.example.treksathi.controller;

import com.example.treksathi.dto.organizer.OrganizerRegistrationDTO;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.service.OrganizerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrganizerController {

    private final OrganizerService organizerService;

    @GetMapping("/api/greet")
    public String greet(HttpServletRequest request){
        return "Hello from trek sathi";
    }

    @GetMapping("/organizer_register")
    public ResponseEntity<String> organizerRegister(@Valid @RequestBody OrganizerRegistrationDTO dto){
        organizerService.registerOrganizer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("User Created Successfully");
    }
}
