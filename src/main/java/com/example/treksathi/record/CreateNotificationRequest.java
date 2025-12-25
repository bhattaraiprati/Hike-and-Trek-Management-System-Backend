package com.example.treksathi.record;

public record CreateNotificationRequest(
        String title,
        String message,
        int userId,
        String type
) {
}
