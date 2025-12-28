package com.example.treksathi.service;

import com.example.treksathi.Interfaces.IFavouriteService;
import com.example.treksathi.dto.favourites.AddFavouriteRequest;
import com.example.treksathi.dto.favourites.FavouriteEventDTO;
import com.example.treksathi.dto.favourites.FavouriteResponse;
import com.example.treksathi.dto.favourites.FavouritesPageResponse;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.*;
import com.example.treksathi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FavouriteServiceImpl implements IFavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final ReviewRepository reviewsRepository;

    @Override
    public FavouriteResponse addToFavourites(String email, AddFavouriteRequest request) {
        User user = getUserByEmail(email);
        Event event = getEventById(request.getEventId());

        // Check if already favourited
        if (favouriteRepository.existsByUserIdAndEventId(user.getId(), event.getId())) {
            throw new RuntimeException("Event is already in your favourites");
        }

        Favourites favourite = Favourites.builder()
                .user(user)
                .event(event)
                .build();

        favourite = favouriteRepository.save(favourite);

        return FavouriteResponse.builder()
                .id(favourite.getId())
                .eventId(event.getId())
                .addedAt(favourite.getAddedAt())
                .message("Event added to favourites successfully")
                .build();
    }

    @Override
    public FavouriteResponse removeFromFavourites(String email, Integer eventId) {
        User user = getUserByEmail(email);

        Favourites favourite = favouriteRepository.findByUserIdAndEventId(user.getId(), eventId)
                .orElseThrow(() -> new RuntimeException("Favourite not found"));

        favouriteRepository.delete(favourite);

        return FavouriteResponse.builder()
                .id(favourite.getId())
                .eventId(eventId)
                .message("Event removed from favourites successfully")
                .build();
    }

    @Override
    public FavouriteResponse toggleFavourite(String email, Integer eventId) {
        User user = getUserByEmail(email);

        if (favouriteRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            // Remove if exists
            return removeFromFavourites(email, eventId);
        } else {
            // Add if not exists
            AddFavouriteRequest request = AddFavouriteRequest.builder()
                    .eventId(eventId)
                    .build();
            return addToFavourites(email, request);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isFavourite(String email, Integer eventId) {
        User user = getUserByEmail(email);
        return favouriteRepository.existsByUserIdAndEventId(user.getId(), eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public FavouritesPageResponse getUserFavourites(String email, Pageable pageable) {
        User user = getUserByEmail(email);

        Page<Favourites> favouritesPage = favouriteRepository
                .findByUserIdOrderByAddedAtDesc(user.getId(), pageable);

        List<EventRegistration> userRegistrations = eventRegistrationRepository
                .findByUserId(user.getId()).orElseThrow();

        // Get user's registered event IDs
        Set<Integer> registeredEventIds = userRegistrations.stream()
                .map(reg -> reg.getEvent().getId())
                .collect(Collectors.toSet());

        // Get ratings for events
        List<Integer> eventIds = favouritesPage.getContent().stream()
                .map(f -> f.getEvent().getId())
                .collect(Collectors.toList());

        Map<Integer, Double> avgRatings = getAverageRatings(eventIds);
        Map<Integer, Integer> ratingCounts = getRatingCounts(eventIds);
        Map<Integer, Integer> participantCounts = getParticipantCounts(eventIds);

        List<FavouriteEventDTO> favouriteDTOs = favouritesPage.getContent().stream()
                .map(favourite -> mapToFavouriteEventDTO(
                        favourite,
                        registeredEventIds,
                        avgRatings,
                        ratingCounts,
                        participantCounts
                ))
                .collect(Collectors.toList());

        return FavouritesPageResponse.builder()
                .favourites(favouriteDTOs)
                .currentPage(favouritesPage.getNumber())
                .totalPages(favouritesPage.getTotalPages())
                .totalItems(favouritesPage.getTotalElements())
                .hasNext(favouritesPage.hasNext())
                .hasPrevious(favouritesPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getUserFavouriteEventIds(String email) {
        User user = getUserByEmail(email);
        return favouriteRepository.findEventIdsByUserId(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getFavouritesCount(String email) {
        User user = getUserByEmail(email);
        return favouriteRepository.countByUserId(user.getId());
    }


    // ============================================
    // Helper Methods
    // ============================================

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Event getEventById(Integer eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    private FavouriteEventDTO mapToFavouriteEventDTO(
            Favourites favourite,
            Set<Integer> registeredEventIds,
            Map<Integer, Double> avgRatings,
            Map<Integer, Integer> ratingCounts,
            Map<Integer, Integer> participantCounts) {

        Event event = favourite.getEvent();
        Integer eventId = event.getId();

        boolean isAvailable = event.getStatus() == EventStatus.ACTIVE
                && event.getDate().isAfter(LocalDate.now());

        boolean isRegistered = registeredEventIds.contains(eventId);

        return FavouriteEventDTO.builder()
                .favouriteId(favourite.getId())
                .eventId(eventId)
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .date(event.getDate())
                .difficulty(event.getDifficultyLevel().name())
                .price(event.getPrice())
                .imageUrl(event.getBannerImageUrl())
                .organizerName(event.getOrganizer().getOrganization_name())
                .maxParticipants(event.getMaxParticipants())
                .currentParticipants(participantCounts.getOrDefault(eventId, 0))
                .rating(avgRatings.getOrDefault(eventId, 0.0))
                .totalRatings(ratingCounts.getOrDefault(eventId, 0))
                .addedAt(favourite.getAddedAt())
                .isAvailable(isAvailable)
                .isRegistered(isRegistered)
                .build();
    }

    private Map<Integer, Double> getAverageRatings(List<Integer> eventIds) {
        if (eventIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        Map<Integer, Double> ratingsMap = new java.util.HashMap<>();
        for (Integer eventId : eventIds) {
            Double avgRating = reviewsRepository.findAverageRatingByEventId(eventId);
            ratingsMap.put(eventId, avgRating != null ? avgRating : 0.0);
        }
        return ratingsMap;
    }

    private Map<Integer, Integer> getRatingCounts(List<Integer> eventIds) {
        if (eventIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        Map<Integer, Integer> countsMap = new java.util.HashMap<>();
        for (Integer eventId : eventIds) {
            int count = reviewsRepository.countByEventsId(eventId);
            countsMap.put(eventId, count);
        }
        return countsMap;
    }

    private Map<Integer, Integer> getParticipantCounts(List<Integer> eventIds) {
        if (eventIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        Map<Integer, Integer> countsMap = new java.util.HashMap<>();
        for (Integer eventId : eventIds) {
            int count = eventRegistrationRepository.countByEventId(eventId);
            countsMap.put(eventId, count);
        }
        return countsMap;
    }
}
