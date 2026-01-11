package com.example.treksathi.dto.ChatBot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponseDTO {
    private String message;              // AI generated text response
    private List<EventCardDTO> events;   // Events to display as cards
    private ResponseType type;           // Type of response

    public enum ResponseType {
        TEXT_ONLY,           // Just show text
        TEXT_WITH_EVENTS,    // Show text + event cards
        EVENTS_ONLY          // Show only event cards
    }
}
