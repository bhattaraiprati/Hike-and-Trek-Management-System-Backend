package com.example.treksathi.record;


public record UpcommingEventRecord(
        int id,
        String title,
        String date,
        String location,
        String organizer,
        String meetingTime,
        int participants,
        String status,
        String bannerImageUrl
) {
}
