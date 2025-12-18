package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.IOrganizerEventService;
import com.example.treksathi.dto.events.*;
import com.example.treksathi.record.EventDetailsOrganizerRecord;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizer/event")
@RequiredArgsConstructor
public class OrganizerEventController {

    private final IOrganizerEventService organizerEventService;

    // CREATE
    @PostMapping("/register-event")
    public ResponseEntity<EventResponseDTO> registerEvent(@Valid @RequestBody EventCreateDTO eventCreateDTO) {
        EventResponseDTO response = organizerEventService.createNewEvent(eventCreateDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // get event by the organizer ID
    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<EventResponseDTO>> getEventsByOrganizer(@PathVariable int organizerId,
                                                                       @AuthenticationPrincipal UserDetails userDetails) {
        List<EventResponseDTO> events = organizerEventService.getEventsByOrganizer(organizerId, userDetails);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/allEventsDetails/{eventId}")
    public ResponseEntity<EventDetailsOrganizerRecord> getAllEventsDetailsByEventId( @PathVariable int eventId,
                                                                                         @AuthenticationPrincipal UserDetails userDetails) {
        EventDetailsOrganizerRecord events = organizerEventService.getAllEventsDetails(eventId, userDetails);
        return ResponseEntity.ok(events);
    }

    @PostMapping("makeAttendance/{eventId}")
    public ResponseEntity<String> makeAttendance(@PathVariable int eventId, @RequestBody List<ParticipantsAttendanceDTO> attendance
                                                 ) {
        organizerEventService.markAttendance(eventId, attendance);
        return ResponseEntity.ok("Attendance marked successfully");
    }

    // Get the event by the Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventResponseDTO>> getEventsByStatus(@PathVariable String status) {
        List<EventResponseDTO> events = organizerEventService.getEventsByStatus(status);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/sendBulkEmail/{eventId}")
    public ResponseEntity<?> sendBulkEmail(@PathVariable int eventId,
                                           @RequestBody EmailAttachmentRequest emailAttachmentRequest) {
        BulkEmailResponse response = organizerEventService.bulkEmailToParticipants(eventId, emailAttachmentRequest);
        return ResponseEntity.ok(response);
    }

    // update the event by the ID
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable int id,
            @Valid @RequestBody EventCreateDTO eventCreateDTO) {
        EventResponseDTO response = organizerEventService.updateEvent(id, eventCreateDTO);
        return ResponseEntity.ok(response);
    }

    // update the event based on the status
    // can be used by the organizer and the admin
    @PatchMapping("/statusChange/{id}")
    public ResponseEntity<EventResponseDTO> updateEventStatus(
            @PathVariable int id,
            @RequestParam String status) {
        EventResponseDTO response = organizerEventService.updateEventStatus(id, status);
        return ResponseEntity.ok(response);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable int id) {
        organizerEventService.deleteEvent(id);
        return ResponseEntity.ok("Event deleted successfully");
    }
}
