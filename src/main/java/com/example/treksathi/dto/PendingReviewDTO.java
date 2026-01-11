package com.example.treksathi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PendingReviewDTO {
    private int eventId;
    private String eventTitle;
    private String eventImage;
    private String organizerName;
    private String completedDate;
    private int daysUntilExpiry;
}
