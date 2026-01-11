package com.example.treksathi.service;

import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VectorService {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    public void addToVectorStore(Event event) {
        if (event.getId() == 0) {
            throw new IllegalArgumentException("Event must have an ID before adding to vector store");
        }

        String content = buildEventTextForEmbedding(event);

        // Generate UUID from event ID for Qdrant compatibility
        String documentId = generateUuidFromEventId(event.getId());

        // Build metadata map - using HashMap for easier construction
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", String.valueOf(event.getId()));
        metadata.put("eventId", String.valueOf(event.getId()));
        metadata.put("type", "EVENT");
        metadata.put("title", event.getTitle());
        metadata.put("organizer", event.getOrganizer().getOrganization_name());
        metadata.put("location", event.getLocation());
        metadata.put("difficulty", event.getDifficultyLevel().name());
        metadata.put("price", String.valueOf(event.getPrice()));
        metadata.put("startDate", event.getDate().toString());
        metadata.put("durationDays", String.valueOf(event.getDurationDays()));
        metadata.put("status", event.getStatus().name());
        metadata.put("maxParticipants", String.valueOf(event.getMaxParticipants()));
        metadata.put("isActive", String.valueOf(event.getStatus() == EventStatus.ACTIVE));
        metadata.put("createdAt", event.getCreatedAt() != null ? event.getCreatedAt().toString() : "");

        // Create document with UUID and metadata
        Document doc = new Document(
                documentId,     // UUID format required by Qdrant
                content,        // Content to embed
                metadata        // Metadata
        );

        // Add to vector store (upsert if ID exists)
        vectorStore.add(List.of(doc));
    }

    public void deleteFromVectorStore(int eventId) {
        String documentId = generateUuidFromEventId(eventId);
        vectorStore.delete(List.of(documentId));
    }

    /**
     * Generate deterministic UUID from event ID
     * This ensures same event ID always generates same UUID
     */
    private String generateUuidFromEventId(int eventId) {
        // Simple approach: pad with zeros to create valid UUID
        return String.format("00000000-0000-0000-0000-%012d", eventId);
    }

    /**
     * Alternative: Generate UUID v5 (SHA-1 based) from event ID
     * More "proper" but same deterministic result
     */
    @SuppressWarnings("unused")
    private String generateUuidV5FromEventId(int eventId) {
        try {
            // Use a namespace UUID for your application
            UUID namespace = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
            String name = "event-" + eventId;

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] namespaceBytes = asBytes(namespace);
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);

            md.update(namespaceBytes);
            md.update(nameBytes);
            byte[] hash = md.digest();

            // Set version (5) and variant bits
            hash[6] &= 0x0f;
            hash[6] |= 0x50;
            hash[8] &= 0x3f;
            hash[8] |= 0x80;

            return asUuid(hash).toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple approach
            return String.format("00000000-0000-0000-0000-%012d", eventId);
        }
    }

    private byte[] asBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (msb >>> (8 * (7 - i)));
            bytes[8 + i] = (byte) (lsb >>> (8 * (7 - i)));
        }
        return bytes;
    }

    private UUID asUuid(byte[] bytes) {
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
            lsb = (lsb << 8) | (bytes[8 + i] & 0xff);
        }
        return new UUID(msb, lsb);
    }

    private String buildEventTextForEmbedding(Event event) {
        LocalDate startDate = event.getDate();
        LocalDate endDate = startDate.plusDays(event.getDurationDays() - 1);

        String difficulty = switch (event.getDifficultyLevel()) {
            case EASY -> "easy / beginner friendly";
            case MODERATE -> "moderate / medium difficulty";
            case DIFFICULT -> "difficult / challenging";
            case EXTREME -> "expert / very challenging";
        };

        return """
            Trekking event: %s
            Organized by: %s
            Location: %s
            Date: from %s to %s (%d days)
            Difficulty level: %s
            Price: %.0f NPR per person
            Max participants: %d
            Meeting point: %s at %s
            Description: %s
            Included services: %s
            Requirements: %s
            """.formatted(
                event.getTitle(),
                event.getOrganizer().getOrganization_name(),
                event.getLocation(),
                startDate,
                endDate,
                event.getDurationDays(),
                difficulty,
                event.getPrice(),
                event.getMaxParticipants(),
                event.getMeetingPoint(),
                event.getMeetingTime() != null ? event.getMeetingTime() : "not specified",
                event.getDescription(),
                String.join(", ", event.getIncludedServices()),
                String.join(", ", event.getRequirements())
        ).trim();
    }
}