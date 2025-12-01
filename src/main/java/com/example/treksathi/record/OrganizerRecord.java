package com.example.treksathi.record;

public record OrganizerRecord(

        int id,
        String organizationName,
        String contactPerson,
        String phone,
        String about,
        String approvalStatus,
        boolean isVerified, // You can derive this from approval_status
        int totalEvents,
        int totalParticipants,
        double rating,
        int reviewCount
) {
}
