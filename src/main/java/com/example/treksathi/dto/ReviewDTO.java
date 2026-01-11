package com.example.treksathi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewDTO {
    private int id;
    private int eventId;
    private String eventTitle;
    private String eventImage;
    private String organizerName;
    private int rating;
    private String comment;
    private String createdAt;
    private int helpfulCount;
    private boolean isHelpful;
}
