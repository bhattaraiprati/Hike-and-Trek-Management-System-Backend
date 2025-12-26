package com.example.treksathi.record;

public record CreateNotificationRequest(
        String title,
        String message,
        String type,
        Integer referenceId,
        String referenceType
) {
}
