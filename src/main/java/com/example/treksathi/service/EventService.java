package com.example.treksathi.service;

import com.example.treksathi.Interfaces.IEventService;
import com.example.treksathi.dto.events.EventResponseDTO;
import com.example.treksathi.dto.pagination.PaginatedResponseDTO;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.exception.EventNotFoundException;

import com.example.treksathi.model.Event;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.record.EventCardResponse;
import com.example.treksathi.record.EventResponseRecord;
import com.example.treksathi.record.OrganizerRecord;
import com.example.treksathi.repository.EventRepository;
import com.example.treksathi.repository.EventRegistrationRepository;
import com.example.treksathi.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventService implements IEventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final ReviewRepository reviewRepository;


    // READ - Get all events
    public PaginatedResponseDTO<EventCardResponse> getAllEvents(int page, int size) {
        Page<EventCardResponse> eventPage = eventRepository.findEventCardsWithParticipantCount(
                EventStatus.ACTIVE,
                PageRequest.of(page, size)
        );

        log.info("Found {} active events on page {} of {}",
                eventPage.getNumberOfElements(),
                eventPage.getNumber() + 1,
                eventPage.getTotalPages());

        return PaginatedResponseDTO.of(eventPage);
    }

    // READ - Get event by ID with organizer details
    public EventResponseRecord getEventById(int id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));

        // Ensure organizer is loaded
        Organizer organizer = event.getOrganizer();

        // Calculate organizer stats
        OrganizerRecord organizerRecord = createOrganizerRecord(organizer);

        // Map event to record with organizer
        return mapEntityToRecord(event, organizerRecord);
    }



    private OrganizerRecord createOrganizerRecord(Organizer organizer) {
        int totalEvents = eventRepository.countByOrganizerId(organizer.getId());
        int totalParticipants = calculateTotalParticipants(organizer.getId());
        ReviewStatus reviewStats = calculateOrganizerRating(organizer.getId());

        return new OrganizerRecord(
                organizer.getId(),
                organizer.getOrganization_name(),
                organizer.getContact_person(),
                organizer.getPhone(),
                organizer.getAbout(),
                organizer.getUser().getProfileImage(),
                organizer.getApproval_status().name(),
                organizer.getApproval_status().name().equals("APPROVED"), // Assuming APPROVED means verified
                totalEvents,
                totalParticipants,
                reviewStats.getAverageRating(),
                reviewStats.getTotalReviews()
        );
    }

    private int calculateTotalParticipants(int organizerId) {
        return eventRegistrationRepository.sumParticipantsByOrganizerId(organizerId);
    }

    private ReviewStatus calculateOrganizerRating(int organizerId) {
        // Query average rating and count for organizer's events
        Double avgRating = reviewRepository.findAverageRatingByOrganizerId(organizerId);
        Long reviewCount = reviewRepository.countByOrganizerId(organizerId);

        return new ReviewStatus(
                avgRating != null ? avgRating : 0.0,
                reviewCount != null ? reviewCount.intValue() : 0
        );
    }

    private EventResponseRecord mapEntityToRecord(Event event, OrganizerRecord organizerRecord) {
        return new EventResponseRecord(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getDate(),
                event.getDurationDays(),
                event.getDifficultyLevel().name(),
                event.getPrice(),
                event.getMaxParticipants(),
                event.getMeetingPoint(),
                event.getMeetingTime(),
                event.getContactPerson(),
                event.getContactEmail(),
                event.getBannerImageUrl(),
                event.getIncludedServices(),
                event.getRequirements(),
                event.getStatus().name(),
                organizerRecord
        );
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

    // Inner class for review stats
    private static class ReviewStatus {
        private final double averageRating;
        private final int totalReviews;

        public ReviewStatus(double averageRating, int totalReviews) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public int getTotalReviews() {
            return totalReviews;
        }
    }
}