package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.search.*;

import java.util.List;

public interface ISearchService {
    SearchResponse searchEvents(SearchCriteria criteria);
    List<QuickSearchSuggestion> getQuickSuggestions(String query);
    List<String> getPopularLocations();
    List<OrganizerSearchDTO> searchOrganizers(String query);
}
