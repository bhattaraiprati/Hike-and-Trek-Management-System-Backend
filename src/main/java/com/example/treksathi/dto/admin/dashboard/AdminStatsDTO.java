package com.example.treksathi.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private long totalUsers;
    private long activeUsers;
    private long totalOrganizers;
    private long pendingOrganizers;
    private long totalEvents;
    private long activeEvents;
    private double totalRevenue;
    private double monthlyRevenue;
    private double revenueGrowth;
    private double userGrowth;
    private double eventGrowth;
}
