package com.example.treksathi.dto.organizer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;

@Data
public class OrganizerDashboardDTO {
    
    private DashboardStats stats;
    private List<UpcomingEventDTO> upcomingEvents;
    private List<RecentRegistrationDTO> recentRegistrations;
    private List<ReviewDTO> reviews;
    private List<NotificationDTO> notifications;
    
    @Data
    public static class DashboardStats {
        private int totalEvents;
        private int totalParticipants;
        private int newReviews;
        private double totalEarnings;
    }
    
    @Data
    public static class UpcomingEventDTO {
        private int id;
        private String title;
        private LocalDate date;
        private LocalTime time;
        private String difficulty;
        private int participants;
        private int maxParticipants;
        private String image;
    }
    
    @Data
    public static class RecentRegistrationDTO {
        private int id;
        private String name;
        private String event;
        private LocalDateTime registrationDate;
        private String contact;
        private String status;
    }
    
    @Data
    public static class ReviewDTO {
        private int id;
        private int rating;
        private String comment;
        private String author;
        private LocalDateTime date;
    }
    
    @Data
    public static class NotificationDTO {
        private int id;
        private String type;
        private String title;
        private String message;
        private LocalDateTime time;
    }
}

