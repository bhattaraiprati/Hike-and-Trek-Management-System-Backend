package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.ISearchService;
import com.example.treksathi.dto.search.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hiker/search")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SearchController {

    private final ISearchService searchService;

    /**
     * Main search endpoint
     * GET /api/search?query=annapurna&difficultyLevel=MODERATE&minPrice=100&maxPrice=500
     */
    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String difficultyLevel,
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
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        log.info("Search request - query: {}, page: {}", query, page);

        SearchCriteria criteria = SearchCriteria.builder()
                .query(query)
                .difficultyLevel(difficultyLevel != null ?
                        com.example.treksathi.enums.DifficultyLevel.valueOf(difficultyLevel) : null)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .startDate(startDate != null ? java.time.LocalDate.parse(startDate) : null)
                .endDate(endDate != null ? java.time.LocalDate.parse(endDate) : null)
                .minDuration(minDuration)
                .maxDuration(maxDuration)
                .location(location)
                .organizerName(organizerName)
                .organizerId(organizerId)
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
