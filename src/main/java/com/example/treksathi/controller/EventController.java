package com.example.treksathi.controller;

import com.example.treksathi.dto.organizer.EventCreateDTO;
import com.example.treksathi.dto.organizer.EventResponseDTO;
import com.example.treksathi.model.Event;
import com.example.treksathi.record.EventResponseRecord;
import com.example.treksathi.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // Get all event
    @GetMapping("/all")
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        List<EventResponseDTO> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }
    // Get Event details by the event ID
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseRecord> getEventById(@PathVariable int id) {
        EventResponseRecord event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }











}
