package com.example.treksathi.controller;

import com.example.treksathi.dto.ReviewDTO;
import com.example.treksathi.dto.PendingReviewDTO;
import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.model.Reviews;
import com.example.treksathi.model.Event;
import com.example.treksathi.repository.ReviewRepository;
import com.example.treksathi.repository.EventRepository;
import com.example.treksathi.repository.UserRepository;
import com.example.treksathi.repository.EventRegistrationRepository; // Assume this exists for bookings
import com.example.treksathi.enums.EventStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    // Get my reviews
    @GetMapping("/my-reviews")
    public ResponseEntity<List<ReviewDTO>> getMyReviews(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        com.example.treksathi.model.User appUser = userRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Reviews> reviews = reviewRepository.findTop5ByUserIdOrderByCreatedAtDesc(appUser.getId());
        List<ReviewDTO> dtoList = reviews.stream().map(this::mapToReviewDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    // Get pending reviews (completed events without review)
    @GetMapping("/pending")
    public ResponseEntity<List<PendingReviewDTO>> getPendingReviews(Authentication authentication) {
        User springUser = (User) authentication.getPrincipal();
        com.example.treksathi.model.User appUser = userRepository.findByEmail(springUser.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get user's completed event registrations
        List<EventRegistration> registrations = eventRegistrationRepository
                .findByUserIdAndEventStatus(appUser.getId(), EventStatus.COMPLETED);

        LocalDate today = LocalDate.now(); // Use LocalDate only
        LocalDate thirtyDaysAgo = today.minusDays(30);

        List<PendingReviewDTO> pending = registrations.stream()
                .map(EventRegistration::getEvent)
                .filter(event -> {
                    LocalDate eventDate = event.getDate();
                    // Event must be in the last 30 days
                    return !eventDate.isBefore(thirtyDaysAgo) && !eventDate.isAfter(today);
                })
                .filter(event ->
                        // User hasn't reviewed this event yet
                        reviewRepository.countByUserIdAndEventsId(appUser.getId(), event.getId()) == 0
                )
                .map(event -> {
                    LocalDate eventDate = event.getDate();
                    LocalDate expiryDate = eventDate.plusDays(30);
                    long daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate);

                    return new PendingReviewDTO(
                            event.getId(),
                            event.getTitle(),
                            event.getBannerImageUrl(),
                            event.getOrganizer().getOrganization_name(),
                            eventDate.toString(), // completedDate
                            (int) Math.max(0, daysUntilExpiry) // ensure non-negative
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(pending);
    }

    // Submit new review
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(
            @RequestBody ReviewDTO dto,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        com.example.treksathi.model.User appUser = userRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Check if can review: completed registration, within 30 days, no existing review
        // ... (add validation)

        Reviews review = new Reviews();
        review.setUser(appUser);
        review.setEvents(event);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Reviews saved = reviewRepository.save(review);
        return ResponseEntity.ok(mapToReviewDTO(saved));
    }

    // Update review
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(
            @PathVariable int id,
            @RequestBody ReviewDTO dto,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        com.example.treksathi.model.User appUser = userRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Reviews review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (review.getUser().getId() != appUser.getId()) {
            throw new RuntimeException("Not authorized");
        }

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        Reviews updated = reviewRepository.save(review);
        return ResponseEntity.ok(mapToReviewDTO(updated));
    }

    private ReviewDTO mapToReviewDTO(Reviews review) {
        return new ReviewDTO(
                review.getId(),
                review.getEvents().getId(),
                review.getEvents().getTitle(),
                review.getEvents().getBannerImageUrl(),
                review.getEvents().getOrganizer().getOrganization_name(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt().toString(),
                0, // helpfulCount - implement if needed
                false // isHelpful - implement if needed
        );
    }
}