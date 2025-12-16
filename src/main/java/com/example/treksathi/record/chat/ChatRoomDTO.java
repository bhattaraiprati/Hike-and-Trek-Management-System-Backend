package com.example.treksathi.record.chat;

public record ChatRoomDTO(
        Long roomId,
        String roomName,
        String roomType,
        String associatedEvent,
        int participantCount
) {
}
