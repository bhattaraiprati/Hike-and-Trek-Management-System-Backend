package com.example.treksathi.record;

import com.example.treksathi.enums.NotificationType;

public record NotificationResponseDTO(
        int id,
        String title,
        String message,
        boolean isRead,
        NotificationType type,
        String createdAt,
        Integer referenceId,
        String referenceType
) {
}
