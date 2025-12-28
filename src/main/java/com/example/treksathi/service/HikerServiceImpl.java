package com.example.treksathi.service;

import com.example.treksathi.Interfaces.IHikerService;
import com.example.treksathi.dto.hiker.*;
import com.example.treksathi.enums.EventRegistrationStatus;
import com.example.treksathi.enums.PaymentStatus;
import com.example.treksathi.model.*;
import com.example.treksathi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HikerServiceImpl implements IHikerService {

    private final UserRepository userRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventRepository eventRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final PaymentRepository paymentsRepository;
    private final ReviewRepository reviewsRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    public HikerDashboardDTO getDashboardData(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return HikerDashboardDTO.builder()
                .userInfo(buildUserInfo(user))
                .stats(getStats(email))
                .upcomingAdventures(getUpcomingAdventures(email))
                .recommendedEvents(getRecommendedEvents(email))
                .recentActivities(getRecentActivity(email))
                .quickActions(buildQuickActions(user))
                .build();
    }

    @Override
    public HikerStatsDTO getStats(String email) {
        User user = getUserByEmail(email);

        List<EventRegistration> registrations = eventRegistrationRepository
                .findByUserIdOrderByRegistrationDateDesc(user.getId());

        long upcomingEvents = registrations.stream()
                .filter(reg -> reg.getEvent().getDate().isAfter(LocalDate.now()))
                .count();

        long completedTrips = registrations.stream()
                .filter(reg -> reg.getEvent().getDate().isBefore(LocalDate.now()))
                .filter(reg -> reg.getStatus() == EventRegistrationStatus.SUCCESS)
                .count();

        double totalDistance = registrations.stream()
                .filter(reg -> reg.getEvent().getDate().isBefore(LocalDate.now()))
                .mapToDouble(reg -> reg.getEvent().getDurationDays() * 15.0) // Estimate 15km per day
                .sum();

        long unreadNotifications = notificationRecipientRepository
                .countByUserIdAndIsReadFalse(user.getId());

        return HikerStatsDTO.builder()
                .upcomingEvents((int) upcomingEvents)
                .completedTrips((int) completedTrips)
                .totalEvents(registrations.size())
                .totalDistance(totalDistance)
                .unreadNotifications((int) unreadNotifications)
                .build();
    }

    @Override
    public List<UpcomingAdventureDTO> getUpcomingAdventures(String email) {
        User user = getUserByEmail(email);

        List<EventRegistration> upcomingRegistrations = eventRegistrationRepository
                .findByUserIdAndEventDateAfterOrderByEventDateAsc(
                        user.getId(),
                        LocalDate.now()
                );

        return upcomingRegistrations.stream()
                .limit(5) // Only next 5 events
                .map(this::mapToUpcomingAdventure)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendedEventDTO> getRecommendedEvents(String email) {
        User user = getUserByEmail(email);

        List<EventRegistration> userRegistrations = eventRegistrationRepository
                .findByUserId(user.getId()).orElseThrow();

        // Get user's registered event IDs to exclude them
        Set<Integer> registeredEventIds = userRegistrations.stream()
                .map(reg -> reg.getEvent().getId())
                .collect(Collectors.toSet());

        // Get user's preferred difficulty levels from past registrations
        Set<String> preferredDifficulties = userRegistrations.stream()
                .map(reg -> reg.getEvent().getDifficultyLevel().name())
                .collect(Collectors.toSet());

        // Find upcoming events not registered by user
        List<Event> availableEvents = eventRepository
                .findByDateAfterAndStatusOrderByDateAsc(
                        LocalDate.now(),
                        com.example.treksathi.enums.EventStatus.ACTIVE
                ).stream()
                .filter(event -> !registeredEventIds.contains(event.getId()))
                .collect(Collectors.toList());

        // Calculate match percentage and map to DTO
        return availableEvents.stream()
                .map(event -> mapToRecommendedEvent(event, preferredDifficulties))
                .sorted((a, b) -> b.getMatchPercentage().compareTo(a.getMatchPercentage()))
                .limit(8)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecentActivityDTO> getRecentActivity(String email) {
        User user = getUserByEmail(email);
        List<RecentActivityDTO> activities = new ArrayList<>();

        // Recent Registrations
        eventRegistrationRepository
                .findTop5ByUserIdOrderByRegistrationDateDesc(user.getId())
                .forEach(reg -> activities.add(RecentActivityDTO.builder()
                        .id(reg.getId())
                        .type("REGISTRATION")
                        .title("Event Registration")
                        .description("Registered for " + reg.getEvent().getTitle())
                        .timestamp(reg.getRegistrationDate())
                        .eventId(reg.getEvent().getId())
                        .isRead(true)
                        .build()));

        // Recent Payments
        paymentsRepository
                .findTop5ByEventRegistrationUserIdOrderByTransactionDateDesc(user.getId())
                .forEach(payment -> activities.add(RecentActivityDTO.builder()
                        .id(payment.getId())
                        .type("PAYMENT")
                        .title("Payment Completed")
                        .description("Paid ₹" + payment.getAmount() + " for " +
                                payment.getEventRegistration().getEvent().getTitle())
                        .timestamp(payment.getTransactionDate())
                        .eventId(payment.getEventRegistration().getEvent().getId())
                        .isRead(true)
                        .build()));

        // Recent Messages (from chat rooms)
        chatMessageRepository
                .findTop5ByChatRoomParticipantsContainingOrderByTimestampDesc(user)
                .forEach(msg -> activities.add(RecentActivityDTO.builder()
                        .id((int) msg.getId())
                        .type("MESSAGE")
                        .title("New Message")
                        .description("Message in " + msg.getChatRoom().getName())
                        .timestamp(msg.getTimestamp())
                        .eventId(msg.getChatRoom().getEvent() != null ?
                                msg.getChatRoom().getEvent().getId() : null)
                        .isRead(false)
                        .build()));

        // Recent Reviews
        reviewsRepository
                .findTop5ByUserIdOrderByCreatedAtDesc(user.getId())
                .forEach(review -> activities.add(RecentActivityDTO.builder()
                        .id(review.getId())
                        .type("REVIEW")
                        .title("Review Submitted")
                        .description("Reviewed " + review.getEvents().getTitle())
                        .timestamp(review.getCreatedAt())
                        .eventId(review.getEvents().getId())
                        .isRead(true)
                        .build()));

        // Sort by timestamp and limit
        return activities.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(10)
                .collect(Collectors.toList());
    }

    // ============================================
    // Helper Methods
    // ============================================

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserInfoDTO buildUserInfo(User user) {
        int completedTrips = eventRegistrationRepository
                .countByUserIdAndEventDateBeforeAndStatus(
                        user.getId(),
                        LocalDate.now(),
                        EventRegistrationStatus.SUCCESS
                );

        String membershipLevel = calculateMembershipLevel(completedTrips);

        return UserInfoDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .avatar(user.getProfileImage())
                .membershipLevel(membershipLevel)
                .streak(calculateStreak(user.getId()))
                .build();
    }

    private String calculateMembershipLevel(int completedTrips) {
        if (completedTrips >= 20) return "EXPERT";
        if (completedTrips >= 10) return "ADVANCED";
        if (completedTrips >= 5) return "INTERMEDIATE";
        return "BEGINNER";
    }

    private Integer calculateStreak(int userId) {
        // Calculate consecutive days of activity
        // This is a simplified version - you may want to implement more complex logic
        List<EventRegistration> recentRegistrations = eventRegistrationRepository
                .findByUserIdOrderByRegistrationDateDesc(userId);

        if (recentRegistrations.isEmpty()) return 0;

        int streak = 0;
        LocalDate lastDate = recentRegistrations.get(0).getRegistrationDate().toLocalDate();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 30 && !lastDate.plusDays(i).isAfter(today); i++) {
            LocalDate checkDate = lastDate.plusDays(i);
            boolean hasActivity = recentRegistrations.stream()
                    .anyMatch(reg -> reg.getRegistrationDate().toLocalDate().equals(checkDate));
            if (hasActivity) streak++;
            else break;
        }

        return streak;
    }

    private UpcomingAdventureDTO mapToUpcomingAdventure(EventRegistration registration) {
        Event event = registration.getEvent();
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), event.getDate());

        String status = "CONFIRMED";
        if (registration.getPayments() == null ||
                registration.getPayments().getPaymentStatus() !=
                        PaymentStatus.SUCCESS) {
            status = "PAYMENT_PENDING";
        }

        return UpcomingAdventureDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .location(event.getLocation())
                .date(event.getDate())
                .difficulty(event.getDifficultyLevel().name())
                .status(status)
                .imageUrl(event.getBannerImageUrl())
                .organizer(event.getOrganizer().getOrganization_name())
                .meetingPoint(event.getMeetingPoint())
                .price(event.getPrice())
                .daysUntil((int) daysUntil)
                .build();
    }

    private RecommendedEventDTO mapToRecommendedEvent(Event event, Set<String> preferredDifficulties) {
        // Calculate match percentage
        int matchPercentage = 50; // Base match
        if (preferredDifficulties.contains(event.getDifficultyLevel().name())) {
            matchPercentage += 30;
        }

        // Get average rating
        Double avgRating = reviewsRepository.findAverageRatingByEventId(event.getId());
        int totalRatings = reviewsRepository.countByEventsId(event.getId());

        if (avgRating != null && avgRating >= 4.5) matchPercentage += 20;

        // Get participant count
        int participants = eventRegistrationRepository.countByEventId(event.getId());

        return RecommendedEventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .location(event.getLocation())
                .startDate(event.getDate())
                .difficulty(event.getDifficultyLevel().name())
                .imageUrl(event.getBannerImageUrl())
                .rating(avgRating != null ? avgRating : 0.0)
                .totalRatings(totalRatings)
                .participants(participants)
                .maxParticipants(event.getMaxParticipants())
                .price(event.getPrice())
                .duration(event.getDurationDays() + " days")
                .matchPercentage(matchPercentage)
                .build();
    }

    private List<QuickActionDTO> buildQuickActions(User user) {
        long unreadMessages = chatMessageRepository.countUnreadMessagesByUser(user.getId());

        return Arrays.asList(
                QuickActionDTO.builder()
                        .id(1)
                        .title("Explore Events")
                        .description("Discover new adventures")
                        .icon("Compass")
                        .path("/hiker-dashboard/explore")
                        .color("bg-gradient-to-r from-blue-500 to-cyan-500")
                        .build(),
                QuickActionDTO.builder()
                        .id(2)
                        .title("Messages")
                        .description("Chat with organizers")
                        .icon("MessageSquare")
                        .path("/hiker-dashboard/messages")
                        .color("bg-gradient-to-r from-green-500 to-emerald-500")
                        .count(unreadMessages > 0 ? (int) unreadMessages : null)
                        .build(),
                QuickActionDTO.builder()
                        .id(3)
                        .title("My Profile")
                        .description("Update your preferences")
                        .icon("User")
                        .path("/hiker-dashboard/profile")
                        .color("bg-gradient-to-r from-purple-500 to-pink-500")
                        .build(),
                QuickActionDTO.builder()
                        .id(4)
                        .title("My Bookings")
                        .description("View all reservations")
                        .icon("Calendar")
                        .path("/hiker-dashboard/events")
                        .color("bg-gradient-to-r from-orange-500 to-red-500")
                        .build()
        );
    }
}
