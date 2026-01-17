package com.example.treksathi.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRecentActivityDTO {
    private int id;
    private String type; // USER_REGISTER, EVENT_CREATE, PAYMENT, REVIEW, REPORT
    private String title;
    private String description;
    private LocalDateTime timestamp;
    private String status;
    private Integer relatedId;
}
