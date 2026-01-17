package com.example.treksathi.controller;

import com.example.treksathi.dto.ChatBot.ChatbotResponseDTO;
import com.example.treksathi.service.RagService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIChatBotController {

    private final RagService ragService;


    /**
     * Main chatbot endpoint - returns text + event cards
     * POST /api/chatbot/ask
     * Body: { "question": "Show me treks in January" }
     */
    @PostMapping("/ask")
    public ResponseEntity<ChatbotResponseDTO> ask(@RequestBody ChatRequest request) {
        ChatbotResponseDTO response = ragService.askWithEventCards(request.getQuestion());
        return ResponseEntity.ok(response);
    }

    /**
     * Simple text-only endpoint (backward compatibility)
     * POST /api/chatbot/ask-simple
     * Body: { "question": "What treks are available?" }
     */
    @PostMapping("/ask-simple")
    public ResponseEntity<String> askSimple(@RequestBody ChatRequest request) {
        String response = ragService.askQuestion(request.getQuestion());
        return ResponseEntity.ok(response);
    }

    /**
     * Search with filters
     * POST /api/chatbot/search
     * Body: { "location": "Everest", "difficulty": "MODERATE", "startDate": "2026-01-01", "endDate": "2026-01-31" }
     */
    @PostMapping("/search")
    public ResponseEntity<ChatbotResponseDTO> searchWithFilters(@RequestBody SearchRequest request) {
        ChatbotResponseDTO response = ragService.searchEventsByFilters(
                request.getLocation(),
                request.getDifficulty(),
                request.getStartDate(),
                request.getEndDate()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get trending/popular events
     * GET /api/chatbot/trending?limit=5
     */
    @GetMapping("/trending")
    public ResponseEntity<ChatbotResponseDTO> getTrending(
            @RequestParam(defaultValue = "6") int limit) {
        ChatbotResponseDTO response = ragService.getTrendingEvents(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Get personalized recommendations
     * POST /api/chatbot/recommendations
     * Body: { "preferences": "I like moderate difficulty treks near Kathmandu" }
     */
    @PostMapping("/recommendations")
    public ResponseEntity<ChatbotResponseDTO> getRecommendations(
            @RequestBody RecommendationRequest request) {
        ChatbotResponseDTO response = ragService.getRecommendations(request.getPreferences());
        return ResponseEntity.ok(response);
    }
}

@Data
class ChatRequest {
    private String question;
}

@Data
class SearchRequest {
    private String location;
    private String difficulty;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}

@Data
class RecommendationRequest {
    private String preferences;
}