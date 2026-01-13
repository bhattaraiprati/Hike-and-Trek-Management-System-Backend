package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.ISearchService;
import com.example.treksathi.dto.search.*;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.exception.UsernameNotFoundException;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.model.User;
import com.example.treksathi.repository.OrganizerRepository;
import com.example.treksathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SearchController {

    private final ISearchService searchService;
    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;

    /**
     * Main search endpoint
     * GET /api/search?query=annapurna&difficultyLevel=MODERATE&minPrice=100&maxPrice=500
     */
    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String difficultyLevel,
            @RequestParam(required = false) String eventStatus,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String organizerName,
            @RequestParam(required = false) Integer organizerId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            Authentication authentication

    ) {
        log.info("Search request - query: {}, page: {}", query, page);

        String username = authentication.getName();
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Find organizer linked to this user
        Organizer organizer = organizerRepository.findByUser(currentUser);
//                .orElseThrow(() -> new RuntimeException("No organizer profile found for this user"));

        SearchCriteria criteria = SearchCriteria.builder()
                .query(query)
                .difficultyLevel(difficultyLevel != null ?
                        com.example.treksathi.enums.DifficultyLevel.valueOf(difficultyLevel) : null)
                .eventStatus(eventStatus != null ?
                        String.valueOf(EventStatus.valueOf(eventStatus.toUpperCase())) : null)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .startDate(startDate != null ? java.time.LocalDate.parse(startDate) : null)
                .endDate(endDate != null ? java.time.LocalDate.parse(endDate) : null)
                .minDuration(minDuration)
                .maxDuration(maxDuration)
                .location(location)
                .organizerName(organizerName)
                .organizerId(organizer.getId())
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        SearchResponse response = searchService.searchEvents(criteria);
        return ResponseEntity.ok(response);
    }

    /**
     * Quick search suggestions (autocomplete)
     * GET /api/search/suggestions?query=ann
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<QuickSearchSuggestion>> getQuickSuggestions(
            @RequestParam String query
    ) {
        log.info("Quick suggestions request: {}", query);
        List<QuickSearchSuggestion> suggestions = searchService.getQuickSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Get popular locations
     * GET /api/search/locations
     */
    @GetMapping("/locations")
    public ResponseEntity<List<String>> getPopularLocations() {
        List<String> locations = searchService.getPopularLocations();
        return ResponseEntity.ok(locations);
    }

    /**
     * Search organizers
     * GET /api/search/organizers?query=pratik
     */
    @GetMapping("/organizers")
    public ResponseEntity<List<OrganizerSearchDTO>> searchOrganizers(
            @RequestParam String query
    ) {
        log.info("Organizer search request: {}", query);
        List<OrganizerSearchDTO> organizers = searchService.searchOrganizers(query);
        return ResponseEntity.ok(organizers);
    }
}
