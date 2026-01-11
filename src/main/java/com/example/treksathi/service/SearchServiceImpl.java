package com.example.treksathi.service;

import com.example.treksathi.Interfaces.ISearchService;
import com.example.treksathi.dto.search.*;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.Event;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.repository.EventRepository;
import com.example.treksathi.repository.OrganizerRepository;
import com.example.treksathi.repository.EventRegistrationRepository;
import com.example.treksathi.repository.ReviewRepository;
import com.example.treksathi.specification.EventSearchSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SearchServiceImpl implements ISearchService {

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final ReviewRepository reviewsRepository;

    @Override
    public SearchResponse searchEvents(SearchCriteria criteria) {
        log.info("Searching events with criteria: {}", criteria);

        // Build sorting
        Sort sort = buildSort(criteria);
        Pageable pageable = PageRequest.of(
                criteria.getPage(),
                criteria.getSize(),
                sort
        );

        // Execute search with specifications
        Page<Event> eventPage = eventRepository.findAll(
                EventSearchSpecification.searchEvents(criteria),
                pageable
        );

        // Map to DTO
        List<SearchResultDTO> results = eventPage.getContent().stream()
                .map(this::mapToSearchResultDTO)
                .collect(Collectors.toList());

        return SearchResponse.builder()
                .results(results)
                .currentPage(eventPage.getNumber())
                .totalPages(eventPage.getTotalPages())
                .totalElements(eventPage.getTotalElements())
                .pageSize(eventPage.getSize())
                .hasNext(eventPage.hasNext())
                .hasPrevious(eventPage.hasPrevious())
                .build();
    }

    @Override
    public List<QuickSearchSuggestion> getQuickSuggestions(String query) {
        List<QuickSearchSuggestion> suggestions = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return suggestions;
        }

        String searchPattern = "%" + query.toLowerCase() + "%";

        // Search events
        List<Event> events = eventRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> {
                    return criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("status"), EventStatus.ACTIVE),
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get("title")),
                                    searchPattern
                            )
                    );
                },
                PageRequest.of(0, 5)
        ).getContent();

        events.forEach(event -> suggestions.add(
                QuickSearchSuggestion.builder()
                        .type("EVENT")
                        .text(event.getTitle())
                        .value(String.valueOf(event.getId()))
                        .icon("calendar")
                        .build()
        ));

        // Search organizers
        List<Organizer> organizers = organizerRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> {
                    return criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("organization_name")),
                            searchPattern
                    );
                },
                PageRequest.of(0, 3)
        ).getContent();

        organizers.forEach(org -> suggestions.add(
                QuickSearchSuggestion.builder()
                        .type("ORGANIZER")
                        .text(org.getOrganization_name())
                        .value(String.valueOf(org.getId()))
                        .icon("user")
                        .build()
        ));

        // Search locations
        List<String> locations = eventRepository.findAll(
                        (root, criteriaQuery, criteriaBuilder) -> {
                            return criteriaBuilder.and(
                                    criteriaBuilder.equal(root.get("status"), EventStatus.ACTIVE),
                                    criteriaBuilder.like(
                                            criteriaBuilder.lower(root.get("location")),
                                            searchPattern
                                    )
                            );
                        }
                ).stream()
                .map(Event::getLocation)
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        locations.forEach(location -> suggestions.add(
                QuickSearchSuggestion.builder()
                        .type("LOCATION")
                        .text(location)
                        .value(location)
                        .icon("map-pin")
                        .build()
        ));

        return suggestions;
    }

    @Override
    public List<String> getPopularLocations() {
        return eventRepository.findAll().stream()
                .filter(e -> e.getStatus() == EventStatus.ACTIVE)
                .map(Event::getLocation)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrganizerSearchDTO> searchOrganizers(String query) {
        String searchPattern = "%" + query.toLowerCase() + "%";

        List<Organizer> organizers = organizerRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> {
                    return criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("organization_name")),
                            searchPattern
                    );
                },
                PageRequest.of(0, 10)
        ).getContent();

        return organizers.stream()
                .map(this::mapToOrganizerSearchDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    private Sort buildSort(SearchCriteria criteria) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        String sortField = switch (criteria.getSortBy().toLowerCase()) {
            case "price" -> "price";
            case "title" -> "title";
            case "popularity" -> "maxParticipants"; // Could be based on registrations
            default -> "date";
        };

        return Sort.by(direction, sortField);
    }

    private SearchResultDTO mapToSearchResultDTO(Event event) {
        int currentParticipants = eventRegistrationRepository
                .countByEventId(event.getId());

        Double avgRating = reviewsRepository
                .findAverageRatingByEventId(event.getId());

        int reviewCount = reviewsRepository
                .countByEventsId(event.getId());

        return SearchResultDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .date(event.getDate())
                .durationDays(event.getDurationDays())
                .difficultyLevel(event.getDifficultyLevel().name())
                .price(event.getPrice())
                .maxParticipants(event.getMaxParticipants())
                .currentParticipants(currentParticipants)
                .bannerImageUrl(event.getBannerImageUrl())
                .status(event.getStatus().name())
                .organizer(mapToOrganizerSearchDTO(event.getOrganizer()))
                .averageRating(avgRating != null ? avgRating : 0.0)
                .reviewCount(reviewCount)
                .build();
    }

    private OrganizerSearchDTO mapToOrganizerSearchDTO(Organizer organizer) {
        int totalEvents = eventRepository.countByOrganizerId(organizer.getId());
        Double avgRating = reviewsRepository.findAverageRatingByOrganizerId(organizer.getId());

        return OrganizerSearchDTO.builder()
                .id(organizer.getId())
                .name(organizer.getContact_person())
                .organizationName(organizer.getOrganization_name())
                .rating(avgRating != null ? avgRating : 0.0)
                .totalEvents(totalEvents)
                .build();
    }
}
