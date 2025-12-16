package com.example.treksathi.record.chat;

import java.time.LocalDateTime;

public record ChatMessageDTO(
        Long chatRoomId,
        String content,
        Long senderId,
        String senderName,
        LocalDateTime timestamp
) {}