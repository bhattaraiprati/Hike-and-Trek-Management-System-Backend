package com.example.treksathi.service;

import com.example.treksathi.dto.event.AdminEventDTO;
import com.example.treksathi.dto.event.EventOrganizerDTO;
import com.example.treksathi.dto.event.EventParticipantDTO;
import com.example.treksathi.dto.event.EventStatsDTO;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.Event;
import com.example.treksathi.model.EventParticipants;
import com.example.treksathi.repository.EventRepository;
import com.example.treksathi.specification.EventSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final EventRepository eventRepository;

    public Page<AdminEventDTO> getAllEvents(int page, int size, String statusStr, String search) {
        Pageable pageable = PageRequest.of(page, size);
        EventStatus status = null;
        if (statusStr != null && !statusStr.equals("ALL")) {
            // Map frontend status to backend status if needed
            // Frontend: PUBLISHED, DRAFT, CANCELLED, COMPLETED
            // Backend: ACTIVE, DRAFT, CANCEL, COMPLETED
            if (statusStr.equals("PUBLISHED")) {
                status = EventStatus.ACTIVE;
            } else if (statusStr.equals("CANCELLED")) {
                status = EventStatus.CANCEL;
            } else {
                try {
                    status = EventStatus.valueOf(statusStr);
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status
                }
            }
        }

        Specification<Event> spec = EventSpecification.getEvents(status, search);
        Page<Event> events = eventRepository.findAll(spec, pageable);
        return events.map(this::mapToAdminEventDTO);
    }

    public EventStatsDTO getEventStats() {
        long totalEvents = eventRepository.count(); // Check if we should filter out DELETED
        long activeEvents = eventRepository
                .count((root, query, cb) -> cb.equal(root.get("status"), EventStatus.ACTIVE));
        long completedEvents = eventRepository
                .count((root, query, cb) -> cb.equal(root.get("status"), EventStatus.COMPLETED));
        long cancelledEvents = eventRepository
                .count((root, query, cb) -> cb.equal(root.get("status"), EventStatus.CANCEL));

        return new EventStatsDTO(totalEvents, activeEvents, completedEvents, cancelledEvents);
    }

    public List<EventParticipantDTO> getEventParticipants(int eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        return event.getEventRegistration().stream()
                .flatMap(reg -> reg.getEventParticipants().stream()
                        .map(p -> mapToEventParticipantDTO(p, reg.getUser().getId(), reg.getUser().getEmail(),
                                reg.getUser().getPhone(), reg.getRegistrationDate())))
                .collect(Collectors.toList());
    }

    public AdminEventDTO updateEventStatus(int id, String statusStr) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        EventStatus newStatus;
        if (statusStr.equals("PUBLISHED")) {
            newStatus = EventStatus.ACTIVE;
        } else if (statusStr.equals("CANCELLED")) {
            newStatus = EventStatus.CANCEL;
        } else {
            try {
                newStatus = EventStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + statusStr);
            }
        }

        event.setStatus(newStatus);
        Event updatedEvent = eventRepository.save(event);
        return mapToAdminEventDTO(updatedEvent);
    }

    private AdminEventDTO mapToAdminEventDTO(Event event) {
        AdminEventDTO dto = new AdminEventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setLocation(event.getLocation());
        dto.setStartDate(event.getDate());
        // For now, assuming endDate is date + durationDays
        dto.setEndDate(event.getDate().plusDays(event.getDurationDays()));
        dto.setPrice(event.getPrice());
        dto.setMaxParticipants(event.getMaxParticipants());

        // Calculate current participants
        int currentParticipants = 0;
        if (event.getEventRegistration() != null) {
            currentParticipants = event.getEventRegistration().stream()
                    .mapToInt(reg -> reg.getEventParticipants().size())
                    .sum();
        }
        dto.setCurrentParticipants(currentParticipants);

        // Map status back to frontend terms
        if (event.getStatus() == EventStatus.ACTIVE) {
            dto.setStatus("PUBLISHED");
        } else if (event.getStatus() == EventStatus.CANCEL) {
            dto.setStatus("CANCELLED");
        } else {
            dto.setStatus(event.getStatus().name());
        }

        dto.setImageUrl(event.getBannerImageUrl());
        dto.setCategory(event.getCategory());
        dto.setDifficultyLevel(event.getDifficultyLevel());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());

        EventOrganizerDTO organizerDTO = new EventOrganizerDTO();
        if (event.getOrganizer() != null) {
            organizerDTO.setId(event.getOrganizer().getId());
            organizerDTO.setName(event.getOrganizer().getUser().getName());
            organizerDTO.setOrganizationName(event.getOrganizer().getOrganization_name());
            organizerDTO.setEmail(event.getOrganizer().getUser().getEmail());
            organizerDTO.setPhone(event.getOrganizer().getUser().getPhone());
        }
        dto.setOrganizer(organizerDTO);

        return dto;
    }

    private EventParticipantDTO mapToEventParticipantDTO(EventParticipants p, int userId, String email, String phone,
            java.time.LocalDateTime bookingDate) {
        EventParticipantDTO dto = new EventParticipantDTO();
        dto.setId(p.getId());
        dto.setUserId(userId);
        dto.setName(p.getName());
        dto.setEmail(email); // Using booker's email for now primarily
        dto.setPhone(phone);
        dto.setBookingDate(bookingDate);
        dto.setStatus("CONFIRMED"); // Logic for participant status if exists
        return dto;
    }
}
