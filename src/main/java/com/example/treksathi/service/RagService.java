package com.example.treksathi.service;

import com.example.treksathi.dto.ChatBot.ChatbotResponseDTO;
import com.example.treksathi.dto.ChatBot.EventCardDTO;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.Event;
import com.example.treksathi.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;
    private final EventRepository eventRepository;


    public ChatbotResponseDTO askWithEventCards(String question) {
        log.info("Processing chatbot question: {}", question);

        try {
            // Step 1: Search for relevant events in vector store
            SearchRequest searchRequest = SearchRequest.query(question)
                    .withTopK(10)  // Get more results for filtering
                    .withSimilarityThreshold(0.60);  // Lower threshold for better recall

            List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);
            log.debug("Found {} relevant documents from vector store", relevantDocs.size());

            // Step 2: Extract event IDs from metadata and filter active events
            List<Integer> eventIds = relevantDocs.stream()
                    .map(doc -> {
                        Object eventIdObj = doc.getMetadata().get("eventId");
                        Object isActiveObj = doc.getMetadata().get("isActive");

                        if (eventIdObj != null) {
                            try {
                                int eventId = Integer.parseInt(eventIdObj.toString());
                                boolean isActive = Boolean.parseBoolean(isActiveObj != null ? isActiveObj.toString() : "false");

                                // Only include active events
                                return isActive ? eventId : null;
                            } catch (NumberFormatException e) {
                                log.warn("Failed to parse event ID: {}", eventIdObj);
                                return null;
                            }
                        }
                        return null;
                    })
                    .filter(id -> id != null)
                    .distinct()
                    .limit(6)  // Limit to 6 events for display
                    .collect(Collectors.toList());

            log.debug("Extracted {} event IDs: {}", eventIds.size(), eventIds);

            // Step 3: Generate AI response with enhanced context
            QuestionAnswerAdvisor qaAdvisor = new QuestionAnswerAdvisor(vectorStore, searchRequest);

            String systemPrompt = buildSystemPrompt(eventIds.size());

            String aiResponse = chatClientBuilder.build()
                    .prompt()
                    .system(systemPrompt)
                    .user(question)
                    .advisors(qaAdvisor)
                    .call()
                    .content();

            log.debug("AI Response generated: {}", aiResponse.substring(0, Math.min(100, aiResponse.length())));

            // Step 4: Fetch full event details for cards
            List<EventCardDTO> eventCards = new ArrayList<>();
            if (!eventIds.isEmpty()) {
                List<Event> events = eventRepository.findAllById(eventIds);

                // Filter out past or cancelled events
                eventCards = events.stream()
                        .filter(event -> event.getStatus() == EventStatus.ACTIVE)
                        .filter(event -> event.getDate().isAfter(LocalDate.now()) ||
                                event.getDate().isEqual(LocalDate.now()))
                        .map(this::mapEventToCardDTO)
                        .collect(Collectors.toList());

                log.info("Returning {} event cards", eventCards.size());
            }

            // Step 5: Determine response type
            ChatbotResponseDTO.ResponseType responseType = determineResponseType(eventCards.size());

            return new ChatbotResponseDTO(aiResponse, eventCards, responseType);

        } catch (Exception e) {
            log.error("Error processing chatbot question", e);
            return new ChatbotResponseDTO(
                    "I apologize, but I encountered an error while searching for events. Please try again.",
                    new ArrayList<>(),
                    ChatbotResponseDTO.ResponseType.TEXT_ONLY
            );
        }
    }

    /**
     * Search events by specific filters (location, difficulty, date range)
     */
    public ChatbotResponseDTO searchEventsByFilters(String location, String difficulty,
                                                    LocalDate startDate, LocalDate endDate) {
        log.info("Searching events with filters - Location: {}, Difficulty: {}, Date: {} to {}",
                location, difficulty, startDate, endDate);

        // Build search query from filters
        StringBuilder queryBuilder = new StringBuilder("Find treks");

        if (location != null && !location.isEmpty()) {
            queryBuilder.append(" in ").append(location);
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            queryBuilder.append(" with ").append(difficulty).append(" difficulty");
        }
        if (startDate != null) {
            queryBuilder.append(" starting from ").append(startDate);
        }

        return askWithEventCards(queryBuilder.toString());
    }

    /**
     * Get trending/popular events
     */
    public ChatbotResponseDTO getTrendingEvents(int limit) {
        log.info("Fetching trending events, limit: {}", limit);

        String question = "Show me popular and trending treks and events";

        ChatbotResponseDTO response = askWithEventCards(question);

        // Limit the results
        if (response.getEvents().size() > limit) {
            response.setEvents(response.getEvents().subList(0, limit));
        }

        return response;
    }

    /**
     * Simple text-only response (backward compatibility)
     */
    public String askQuestion(String question) {
        log.info("Processing simple question: {}", question);

        QuestionAnswerAdvisor qaAdvisor = new QuestionAnswerAdvisor(vectorStore);

        return chatClientBuilder.build()
                .prompt()
                .system("""
                        You are a helpful assistant for trekking and events in Nepal.
                        Answer based on the provided context from the vector store.
                        If information is not available, respond with:
                        "Sorry, I don't have information about that trek/event."
                        Keep answers concise, friendly and informative.
                        """)
                .user(question)
                .advisors(qaAdvisor)
                .call()
                .content();
    }

    /**
     * Get event recommendations based on user preferences
     */
    public ChatbotResponseDTO getRecommendations(String userPreferences) {
        log.info("Getting recommendations for: {}", userPreferences);

        String enhancedQuery = String.format(
                "Based on these preferences: %s, recommend suitable treks and events",
                userPreferences
        );

        return askWithEventCards(enhancedQuery);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Build system prompt based on context
     */
    private String buildSystemPrompt(int eventsFound) {
        if (eventsFound > 0) {
            return """
                    You are TrekSathi AI, a friendly assistant for trekking and outdoor events in Nepal.
                    
                    Guidelines:
                    - Be warm, enthusiastic, and helpful
                    - When events are found, mention that you've found some matching treks
                    - Do NOT list all event details (dates, prices, locations) - those will be shown in cards
                    - Keep your response brief (2-3 sentences max)
                    - Focus on being conversational and helpful
                    - Use emojis sparingly to add friendliness
                    
                    Example responses:
                    - "I found some exciting treks that match what you're looking for! 🏔️"
                    - "Great choice! Here are some treks in that area."
                    - "I've got a few options for you based on your preferences!"
                    """;
        } else {
            return """
                    You are TrekSathi AI, a friendly assistant for trekking and outdoor events in Nepal.
                    
                    No matching events were found. Guidelines:
                    - Politely inform the user that no treks match their criteria
                    - Suggest they try different dates, locations, or difficulty levels
                    - Offer to help with other queries
                    - Be encouraging and helpful
                    
                    Example responses:
                    - "I couldn't find any treks matching those specific criteria. Would you like to try different dates or locations?"
                    - "No events available for that timeframe yet. Can I help you find treks in a different month?"
                    """;
        }
    }

    /**
     * Determine response type based on events found
     */
    private ChatbotResponseDTO.ResponseType determineResponseType(int eventCount) {
        if (eventCount == 0) {
            return ChatbotResponseDTO.ResponseType.TEXT_ONLY;
        } else if (eventCount <= 2) {
            return ChatbotResponseDTO.ResponseType.TEXT_WITH_EVENTS;
        } else {
            return ChatbotResponseDTO.ResponseType.TEXT_WITH_EVENTS;
        }
    }

    /**
     * Map Event entity to EventCardDTO for frontend display
     */
    private EventCardDTO mapEventToCardDTO(Event event) {
        EventCardDTO dto = new EventCardDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setLocation(event.getLocation());
        dto.setDate(event.getDate().toString());
        dto.setDurationDays(event.getDurationDays());
        dto.setPrice(event.getPrice());
        dto.setDifficulty(formatDifficulty(event.getDifficultyLevel().name()));
        dto.setOrganizer(event.getOrganizer().getOrganization_name());
        dto.setBannerImageUrl(event.getBannerImageUrl());
        dto.setMaxParticipants(event.getMaxParticipants());
        dto.setStatus(event.getStatus().name());

        return dto;
    }

    /**
     * Format difficulty level for display
     */
    private String formatDifficulty(String difficulty) {
        return switch (difficulty) {
            case "EASY" -> "Easy";
            case "MODERATE" -> "Moderate";
            case "DIFFICULT" -> "Difficult";
            case "EXTREME" -> "Extreme";
            default -> difficulty;
        };
    }
}