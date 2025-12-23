package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.events.*;
import com.example.treksathi.record.EventDetailsOrganizerRecord;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface IOrganizerEventService {
    EventResponseDTO createNewEvent(EventCreateDTO eventCreateDTO);

    // READ
    List<EventResponseDTO> getEventsByOrganizer(int organizerId, UserDetails userDetails);

    EventDetailsOrganizerRecord getAllEventsDetails(int eventId, UserDetails userDetails);

    List<EventResponseDTO> getEventsByStatus(String status);


    void cancelEventRegistration(int id);

    // UPDATE
    EventResponseDTO updateEvent(int id, EventCreateDTO eventCreateDTO);

    EventResponseDTO updateEventStatus(int id, String status);

    void markAttendance(int eventId, List<ParticipantsAttendanceDTO> attendanceList);

    // EMAIL
    BulkEmailResponse bulkEmailToParticipants(
            int eventId,
            EmailAttachmentRequest emailAttachmentRequest
    );

    // DELETE
    void deleteEvent(int id);
}
