package com.example.treksathi.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    private AdminStatsDTO stats;
    private List<RevenueAnalyticsDTO> revenueChart;
    private List<UserGrowthDTO> userGrowth;
    private List<AdminRecentActivityDTO> recentActivities;
}
