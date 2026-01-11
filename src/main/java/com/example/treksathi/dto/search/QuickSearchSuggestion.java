package com.example.treksathi.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickSearchSuggestion {
    private String type; // EVENT, ORGANIZER, LOCATION
    private String text;
    private String value;
    private String icon;
}
