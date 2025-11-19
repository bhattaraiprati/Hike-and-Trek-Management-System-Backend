package com.example.treksathi.controller;

import com.example.treksathi.dto.organizer.EventCreateDTO;
import com.example.treksathi.dto.organizer.EventResponseDTO;
import com.example.treksathi.model.Event;
import com.example.treksathi.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // CREATE
    @PostMapping("/register-event")
    public ResponseEntity<EventResponseDTO> registerEvent(@Valid @RequestBody EventCreateDTO eventCreateDTO) {
        EventResponseDTO response = eventService.createNewEvent(eventCreateDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        List<EventResponseDTO> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable int id) {
        EventResponseDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<EventResponseDTO>> getEventsByOrganizer(@PathVariable int organizerId) {
        List<EventResponseDTO> events = eventService.getEventsByOrganizer(organizerId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventResponseDTO>> getEventsByStatus(@PathVariable String status) {
        List<EventResponseDTO> events = eventService.getEventsByStatus(status);
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable int id,
            @Valid @RequestBody EventCreateDTO eventCreateDTO) {
        EventResponseDTO response = eventService.updateEvent(id, eventCreateDTO);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EventResponseDTO> updateEventStatus(
            @PathVariable int id,
            @RequestParam String status) {
        EventResponseDTO response = eventService.updateEventStatus(id, status);
        return ResponseEntity.ok(response);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable int id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok("Event deleted successfully");
    }


}
