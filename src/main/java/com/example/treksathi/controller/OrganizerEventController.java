package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.IOrganizerEventService;
import com.example.treksathi.dto.events.*;
import com.example.treksathi.dto.organizer.StatusUpdateRequest;
import com.example.treksathi.record.EventDetailsOrganizerRecord;
import com.example.treksathi.service.OrganizerEventService;
import com.example.treksathi.service.VectorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
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
@Tag(name = "Organizer Event Apis", description = "APIs related to Organizer Event Management")
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

    /**
     * Get all events created by the current authenticated organizer
     * GET /api/organizer/events?status=PENDING&search=annapurna&page=0&size=10
     */
//    @GetMapping
//    public ResponseEntity<Page<EventDTO>> getMyEvents(
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) String search,
//            @RequestParam(required = false) String difficultyLevel,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "createdAt") String sortBy,
//            @RequestParam(defaultValue = "DESC") String sortDir
//    ) {
//        Long organizerId = authFacade.getCurrentUserId(); // ← very important!
//
//        EventFilterCriteria criteria = EventFilterCriteria.builder()
//                .organizerId(organizerId)
//                .search(search)
//                .status(status != null ? EventStatus.valueOf(status.toUpperCase()) : null)
//                .difficultyLevel(difficultyLevel != null ?
//                        DifficultyLevel.valueOf(difficultyLevel.toUpperCase()) : null)
//                .page(page)
//                .size(size)
//                .sortBy(sortBy)
//                .sortDirection(sortDir)
//                .build();
//
//        Page<Event> eventsPage = organizerEventService.findOrganizerEvents(criteria);
//
//        Page<EventDTO> dtoPage = eventsPage.map(this::convertToDTO);
//
//        return ResponseEntity.ok(dtoPage);
//    }

    @PostMapping("makeAttendance/{eventId}")
    public ResponseEntity<String> makeAttendance(@PathVariable int eventId, @RequestBody List<ParticipantsAttendanceDTO> attendance
                                                 ) {
        organizerEventService.markAttendance(eventId, attendance);
        return ResponseEntity.ok("Attendance marked successfully");
    }
    @DeleteMapping("/cancel/registration/{id}")
    public ResponseEntity<Void> cancelEventRegistration(@PathVariable int id){
        organizerEventService.cancelEventRegistration(id);
        return ResponseEntity.noContent().build();
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
            @RequestBody StatusUpdateRequest request) {  // New DTO for body

        EventResponseDTO response = organizerEventService.updateEventStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable int id) {
        organizerEventService.deleteEvent(id);
        return ResponseEntity.ok("Event deleted successfully");
    }
}
