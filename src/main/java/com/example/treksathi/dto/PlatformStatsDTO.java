package com.example.treksathi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformStatsDTO {
    private long totalTrails;
    private long communityMembers;
    private long verifiedOrganizers;
}
