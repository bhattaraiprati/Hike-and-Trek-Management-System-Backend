package com.example.treksathi.dto.organizer;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class OrganizerProfileDTO {

    private int id;
    private String name;
    private String email;
    private String phone;
    private String bio;
    private String location;
    private String website;
    private int experienceYears;
    private List<String> specialization;
    private String profileImage;
    private String bannerImage;

    private int totalEvents;
    private int completedEvents;
    private int upcomingEvents;
    private int totalParticipants;
    private double averageRating;
    private LocalDateTime memberSince;
    private String verificationStatus;

    private SocialLinks socialLinks;
    private Stats stats;

    @Data
    public static class SocialLinks {
        private String facebook;
        private String instagram;
        private String twitter;
        private String linkedin;
    }

    @Data
    public static class Stats {
        private double totalRevenue;
        private int repeatClients;
        private double satisfactionRate;
    }
}


