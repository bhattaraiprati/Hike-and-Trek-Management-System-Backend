package com.example.treksathi.service;

import com.example.treksathi.dto.organizer.EventCreateDTO;
import com.example.treksathi.dto.organizer.EventResponseDTO;
import com.example.treksathi.enums.DifficultyLevel;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.Event;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.model.User;
import com.example.treksathi.repository.EventRepository;
import com.example.treksathi.repository.OrganizerRepository;
import com.example.treksathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * CREATE - Create a new event
     * Gets the organizer from the authenticated user context
     */
    @Transactional
    public EventResponseDTO createNewEvent(EventCreateDTO eventCreateDTO) {
        // Get authenticated organizer
        User user = getAuthenticatedOrganizer();

        Organizer organizer = organizerRepository.findByUser(Optional.ofNullable(user));

        Event event = new Event();
        event.setOrganizer(organizer);

        // Map DTO to Entity
        mapDtoToEntity(eventCreateDTO, event);

        // Set system fields
        event.setStatus(EventStatus.PENDING);
        event.setCreatedAt(LocalDateTime.now().format(formatter));
        event.setUpdatedAt(LocalDateTime.now().format(formatter));

        // Save event
        Event savedEvent = eventRepository.save(event);

        return mapEntityToDto(savedEvent);
    }

    /**
     * READ - Get all events
     */
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    /**
     * READ - Get event by ID
     */
    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(int id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return mapEntityToDto(event);
    }

    /**
     * READ - Get all events created by a specific organizer
     */
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getEventsByOrganizer(int organizerId) {
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("Organizer not found with id: " + organizerId));

        List<Event> events = eventRepository.findByOrganizer(organizer);
        return events.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    /**
     * READ - Get events by status (PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED)
     */
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getEventsByStatus(String status) {
        try {
            EventStatus eventStatus = EventStatus.valueOf(status.toUpperCase());
            List<Event> events = eventRepository.findByStatus(eventStatus);
            return events.stream()
                    .map(this::mapEntityToDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status + ". Valid statuses are: PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED");
        }
    }

    /**
     * UPDATE - Update an existing event
     * Only the organizer who created the event can update it
     */
    @Transactional
    public EventResponseDTO updateEvent(int id, EventCreateDTO eventCreateDTO) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        // Check if the authenticated user is the owner of this event

        User user = getAuthenticatedOrganizer();
        Organizer authenticatedOrganizer = organizerRepository.findByUser(Optional.ofNullable(user));


        if (event.getOrganizer().getId() != authenticatedOrganizer.getId()) {
            throw new RuntimeException("You are not authorized to update this event");
        }

        // Update fields
        mapDtoToEntity(eventCreateDTO, event);
        event.setUpdatedAt(LocalDateTime.now().format(formatter));

        Event updatedEvent = eventRepository.save(event);
        return mapEntityToDto(updatedEvent);
    }

    /**
     * UPDATE - Update only the event status
     * This can be used by admins or organizers
     */
    @Transactional
    public EventResponseDTO updateEventStatus(int id, String status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        try {
            EventStatus eventStatus = EventStatus.valueOf(status.toUpperCase());
            event.setStatus(eventStatus);
            event.setUpdatedAt(LocalDateTime.now().format(formatter));

            Event updatedEvent = eventRepository.save(event);
            return mapEntityToDto(updatedEvent);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status + ". Valid statuses are: PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED");
        }
    }

    /**
     * DELETE - Delete an event
     * Only the organizer who created the event can delete it
     */
    @Transactional
    public void deleteEvent(int id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        // Check if the authenticated user is the owner of this event
        User user = getAuthenticatedOrganizer();
        Organizer authenticatedOrganizer = organizerRepository.findByUser(Optional.ofNullable(user));

        if (event.getOrganizer().getId() != authenticatedOrganizer.getId()) {
            throw new RuntimeException("You are not authorized to delete this event");
        }

        eventRepository.delete(event);
    }

    private User getAuthenticatedOrganizer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        // Get the username/email from authentication
        String username = authentication.getName();

        // Find organizer by username/email
        // Modify this based on your Organizer entity structure
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Organizer not found for authenticated user"));

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
            throw new RuntimeException("Invalid difficulty level: " + dto.getDifficultyLevel() +
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
