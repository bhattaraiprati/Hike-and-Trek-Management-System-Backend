package com.example.treksathi.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerSearchDTO {
    private Integer id;
    private String name;
    private String organizationName;
    private Double rating;
    private Integer totalEvents;
}
