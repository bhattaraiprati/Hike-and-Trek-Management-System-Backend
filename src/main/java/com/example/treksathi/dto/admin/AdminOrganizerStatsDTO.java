package com.example.treksathi.dto.admin;

import lombok.Data;

@Data
public class AdminOrganizerStatsDTO {
    private long totalOrganizers;
    private long pendingCount;
    private long approvedCount;
    private long rejectedCount;
}
