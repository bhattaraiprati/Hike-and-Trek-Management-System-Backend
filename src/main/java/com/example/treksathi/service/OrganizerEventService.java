package com.example.treksathi.service;

import com.example.treksathi.Interfaces.IEmailSendService;
import com.example.treksathi.Interfaces.IOrganizerEventService;
import com.example.treksathi.dto.events.*;
import com.example.treksathi.enums.DifficultyLevel;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.exception.*;
import com.example.treksathi.mapper.EventResponseMapper;
import com.example.treksathi.model.Event;
import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.model.User;
import com.example.treksathi.record.EventDetailsOrganizerRecord;
import com.example.treksathi.repository.EventRepository;
import com.example.treksathi.repository.OrganizerRepository;
import com.example.treksathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizerEventService implements IOrganizerEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;
    private final EventResponseMapper eventResponseMapper;
    private final IEmailSendService emailSendService;
    private final VectorService vectorService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public EventResponseDTO createNewEvent(EventCreateDTO eventCreateDTO) {
        // Get authenticated organizer
        User user = getAuthenticatedOrganizer();

        Organizer organizer = organizerRepository.findByUser(user);

        Event event = new Event();
        event.setOrganizer(organizer);

        // Map DTO to Entity
        mapDtoToEntity(eventCreateDTO, event);

        event.setStatus(EventStatus.ACTIVE);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);
        vectorService.addToVectorStore(savedEvent);

        return mapEntityToDto(savedEvent);
    }

    public List<EventResponseDTO> getEventsByOrganizer(int organizerId, UserDetails userDetails)  {
        Organizer organizer = organizerRepository.findByUserId(organizerId)
                .orElseThrow(() -> new NotFoundException("Organizer not found with id: " + organizerId));

        String authenticatedUsername = userDetails.getUsername();
        String organizerOwnerUsername = organizer.getUser().getEmail();
        if (!organizerOwnerUsername.equals(authenticatedUsername)) {
            throw new UnauthorizedException("You are not authorized to view events for this organizer");
        }
        List<Event> events = eventRepository.findByOrganizer(organizer);
        return events.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public EventDetailsOrganizerRecord getAllEventsDetails(int eventId, UserDetails userDetails) {
        String authenticatedUsername = userDetails.getUsername();
        User user = userRepository.findByEmail(authenticatedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + authenticatedUsername));

        Organizer organizer = organizerRepository.findByUser(user);
        if (organizer == null) {
            throw new NotFoundException("Organizer not found for user: " + authenticatedUsername);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        // Verify that this organizer owns this event (security check)
        if (event.getOrganizer().getId() != organizer.getId()) {
            throw new UnauthorizedException("Unauthorized: You are not the organizer of this event");
        }

        // Map the event to the record (this includes all registrations)
        return eventResponseMapper.toEventDetailsOrganizerRecord(event);
    }

    public void markAttendance(int eventId, List<ParticipantsAttendanceDTO> attendanceList) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        // Iterate through the attendance list and update each participant's attendance status
        for (ParticipantsAttendanceDTO attendanceDTO : attendanceList) {
            var participant = event.getEventRegistration().stream()
                    .flatMap(reg -> reg.getEventParticipants().stream())
                    .filter(p -> p.getId() == attendanceDTO.getParticipantId())
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Participant not found with id: " + attendanceDTO.getParticipantId()));

            participant.setAttendanceStatus(attendanceDTO.getAttendanceStatus());
        }

        // Save the updated event (which cascades to participants)
        eventRepository.save(event);
    }
    //     READ - Get events by status (PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED)
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getEventsByStatus(String status) {
        try {
            EventStatus eventStatus = EventStatus.valueOf(status.toUpperCase());
            List<Event> events = eventRepository.findByStatus(eventStatus);
            return events.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new InvalidCredentialsException("Invalid status: " + status + ". Valid statuses are: PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED");
        }
    }


    public void cancelEventRegistration(int id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        event.setStatus(EventStatus.DELETED);
        vectorService.deleteFromVectorStore(event.getId());
        eventRepository.save(event);
    }

    //     UPDATE - Update an existing event
//     Only the organizer who created the event can update it
    @Transactional
    public EventResponseDTO updateEvent(int id, EventCreateDTO eventCreateDTO) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        User user = getAuthenticatedOrganizer();
        Organizer authenticatedOrganizer = organizerRepository.findByUser(user);
//        vectorService.deleteFromVectorStore(event.getId());

        if (event.getOrganizer().getId() != authenticatedOrganizer.getId()) {
            throw new UnauthorizedException("You are not authorized to update this event");
        }

        mapDtoToEntity(eventCreateDTO, event);
        event.setUpdatedAt(LocalDateTime.now());

        Event updatedEvent = eventRepository.save(event);
        vectorService.addToVectorStore(updatedEvent);
        return mapEntityToDto(updatedEvent);
    }

    //     UPDATE - Update only the event status
//     This can be used by admins and organizers
    @Transactional
    public EventResponseDTO updateEventStatus(int id, String status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        vectorService.deleteFromVectorStore(event.getId());
        try {
            EventStatus eventStatus = EventStatus.valueOf(status.toUpperCase());
            event.setStatus(eventStatus);
            event.setUpdatedAt(LocalDateTime.now());

            Event updatedEvent = eventRepository.save(event);
            vectorService.addToVectorStore(updatedEvent);
            return mapEntityToDto(updatedEvent);
        } catch (IllegalArgumentException e) {
            throw new InvalidCredentialsException("Invalid status: " + status + ". Valid statuses are: PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED");
        }
    }

    public BulkEmailResponse bulkEmailToParticipants(int eventId, EmailAttachmentRequest emailAttachmentRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        List<String> registeredEmails = event.getEventRegistration().stream()
                .map(EventRegistration::getEmail)
                .filter(email -> email != null && !email.isEmpty())
                .distinct()
                .toList();

        List<String> requestedRecipients = emailAttachmentRequest.getRecipients();

        List<String> validRecipients = requestedRecipients.stream()
                .filter(registeredEmails::contains)
                .distinct()
                .toList();

        List<String> invalidRecipients = requestedRecipients.stream()
                .filter(email -> !registeredEmails.contains(email))
                .toList();

        if (validRecipients.isEmpty()) {
            throw new InvalidCredentialsException("No valid recipients found. All provided emails are not registered for this event.");
        }

        emailSendService.sendBulkEmailAsync(
                validRecipients,
                emailAttachmentRequest.getSubject(),
                emailAttachmentRequest.getText()
        );

        return new BulkEmailResponse(
                requestedRecipients.size(),
                validRecipients.size(),
                invalidRecipients.size(),
                invalidRecipients,
                "Bulk email sent successfully to " + validRecipients.size() + " recipients"
        );
    }

    //     DELETE - Delete an event
//     Only the organizer who created the event can delete it
    @Transactional
    public void deleteEvent(int id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));

        // Check if the authenticated user is the owner of this event
        User user = getAuthenticatedOrganizer();
        Organizer authenticatedOrganizer = organizerRepository.findByUser(user);

        if (event.getOrganizer().getId() != authenticatedOrganizer.getId()) {
            throw new UnauthorizedException("You are not authorized to delete this event");
        }

        eventRepository.delete(event);
    }

    private User getAuthenticatedOrganizer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("User is not authenticated");
        }

        // Get the email from authentication
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("Organizer not found for authenticated user"));

        return user;
    }

    private void mapDtoToEntity(EventCreateDTO dto, Event event) {
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setLocation(dto.getLocation());
        event.setDate(dto.getDate());
        event.setDurationDays(dto.getDurationDays());

        try {
            event.setDifficultyLevel(DifficultyLevel.valueOf(dto.getDifficultyLevel().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new InvalidCredentialsException("Invalid difficulty level: " + dto.getDifficultyLevel() +
                    ". Valid levels are: EASY, MODERATE, DIFFICULT, EXTREME");
        }

        event.setPrice(dto.getPrice());
        event.setMaxParticipants(dto.getMaxParticipants());
        event.setMeetingPoint(dto.getMeetingPoint());
        event.setMeetingTime(dto.getMeetingTime());
        event.setContactPerson(dto.getContactPerson());
        event.setContactEmail(dto.getContactEmail());
        event.setBannerImageUrl(dto.getBannerImageUrl());
        event.setIncludedServices(dto.getIncludedServices());
        event.setRequirements(dto.getRequirements());
    }

    private EventResponseDTO mapEntityToDto(Event event) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setLocation(event.getLocation());
        dto.setDate(event.getDate());
        dto.setDurationDays(event.getDurationDays());
        dto.setDifficultyLevel(event.getDifficultyLevel().name());
        dto.setPrice(event.getPrice());
        dto.setMaxParticipants(event.getMaxParticipants());
        dto.setMeetingPoint(event.getMeetingPoint());
        dto.setMeetingTime(event.getMeetingTime());
        dto.setContactPerson(event.getContactPerson());
        dto.setContactEmail(event.getContactEmail());
        dto.setBannerImageUrl(event.getBannerImageUrl());
        dto.setIncludedServices(event.getIncludedServices());
        dto.setRequirements(event.getRequirements());
        dto.setStatus(event.getStatus().name());
        return dto;
    }
}
