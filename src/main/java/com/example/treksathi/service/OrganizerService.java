package com.example.treksathi.service;

import java.util.List;

import com.example.treksathi.model.*;
import com.example.treksathi.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.treksathi.Interfaces.IOrganizerService;
import com.example.treksathi.Interfaces.IUserServices;
import com.example.treksathi.dto.organizer.OrganizerDashboardDTO;
import com.example.treksathi.dto.organizer.OrganizerProfileDTO;
import com.example.treksathi.dto.organizer.OrganizerRegistrationDTO;
import com.example.treksathi.enums.AccountStatus;
import com.example.treksathi.enums.Approval_status;
import com.example.treksathi.enums.AuthProvidertype;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.enums.PaymentStatus;
import com.example.treksathi.enums.Role;
import com.example.treksathi.exception.InternalServerErrorException;
import com.example.treksathi.exception.NotFoundException;
import com.example.treksathi.exception.UserAlreadyExistException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizerService implements IOrganizerService {

    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;
    private final IUserServices userServicesl;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Organizer registerOrganizer(OrganizerRegistrationDTO dto){

        try{
            User existingUser  = userRepository.findByEmail(dto.getEmail()).orElse(null);
            if(existingUser != null ){
                throw new UserAlreadyExistException("User already exist for "+ dto.getEmail());
            }

            String hashedPassword = passwordEncoder.encode(dto.getPassword());
            User user = new User();
            user.setName(dto.getFullName());
            user.setEmail(dto.getEmail());
            user.setPassword(hashedPassword);
            user.setPhone(dto.getPhone());
            user.setRole(Role.ORGANIZER);
            user.setProviderType(AuthProvidertype.LOCAL);
            user.setStatus(AccountStatus.ACTIVE);
            user = userRepository.save(user);

            Organizer organizer = new Organizer();
            organizer.setUser(user);
            organizer.setOrganization_name(dto.getOrganizationName());
            organizer.setContact_person(dto.getFullName());
            organizer.setAddress(dto.getAddress());
            organizer.setPhone(dto.getPhone());
            organizer.setAbout(dto.getAbout());
            organizer.setDocument_url(dto.getDocumentUrl());
            organizer.setApprovalStatus(Approval_status.SUCCESS);
            organizer = organizerRepository.save(organizer);
            try{
                userServicesl.sendRegistrationOTP(user);
            }catch (Exception e){
                log.error("Failed to initiate OTP sending for user: {}", user.getId(), e);
            }


            return organizer;
        }
        catch(Exception e){
            throw new InternalServerErrorException("Failed to Create User");
        }
    }

    @Override
    public OrganizerProfileDTO getOrganizerProfile(int userId) {
        Organizer organizer = organizerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Organizer not found for user id: " + userId));
        return mapToProfileDTO(organizer);
    }

    @Override
    @Transactional
    public OrganizerProfileDTO updateOrganizerProfile(int userId, OrganizerProfileDTO profileDTO) {
        Organizer organizer = organizerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Organizer not found for user id: " + userId));

        User user = organizer.getUser();

        if (profileDTO.getName() != null && !profileDTO.getName().isBlank()) {
            user.setName(profileDTO.getName());
        }
        if (profileDTO.getProfileImage() != null && !profileDTO.getProfileImage().isBlank()) {
            user.setProfileImage(profileDTO.getProfileImage());
        }

        if (profileDTO.getPhone() != null && !profileDTO.getPhone().isBlank()) {
            user.setPhone(profileDTO.getPhone());
            organizer.setPhone(profileDTO.getPhone());
        }

        if (profileDTO.getBio() != null) {
            organizer.setAbout(profileDTO.getBio());
        }

        if (profileDTO.getLocation() != null) {
            organizer.setAddress(profileDTO.getLocation());
        }
        if (profileDTO.getBannerImage() != null) {
            organizer.setCover_image(profileDTO.getBannerImage());
        }

        // Persist basic updates
        userRepository.save(user);
        organizerRepository.save(organizer);

        return mapToProfileDTO(organizer);
    }

    private OrganizerProfileDTO mapToProfileDTO(Organizer organizer) {
        User user = organizer.getUser();
        int organizerId = organizer.getId();

        int totalEvents = eventRepository.countByOrganizerId(organizerId);
        int completedEvents = eventRepository.countByOrganizerIdAndStatus(organizerId, EventStatus.COMPLETED);
        int upcomingEvents = eventRepository.countUpcomingEventsByOrganizerIdAndStatus(organizerId, EventStatus.ACTIVE);
        int totalParticipants = eventRegistrationRepository.sumParticipantsByOrganizerId(organizerId);

        Double avgRating = reviewRepository.findAverageRatingByOrganizerId(organizerId);

        OrganizerProfileDTO dto = new OrganizerProfileDTO();
        dto.setId(organizerId);
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(organizer.getPhone() != null ? organizer.getPhone() : user.getPhone());
        dto.setBio(organizer.getAbout());
        dto.setLocation(organizer.getAddress());
        dto.setWebsite(null);
        dto.setExperienceYears(0);
        dto.setSpecialization(List.of());
        dto.setProfileImage(user.getProfileImage());
        dto.setBannerImage(organizer.getCover_image());

        dto.setTotalEvents(totalEvents);
        dto.setCompletedEvents(completedEvents);
        dto.setUpcomingEvents(upcomingEvents);
        dto.setTotalParticipants(totalParticipants);
        dto.setAverageRating(avgRating != null ? avgRating : 0.0);
        dto.setMemberSince(organizer.getCreatedAt());
        dto.setVerificationStatus(organizer.getApprovalStatus().name());

        OrganizerProfileDTO.Stats stats = new OrganizerProfileDTO.Stats();
        stats.setTotalRevenue(0.0);
        stats.setRepeatClients(0);
        stats.setSatisfactionRate(0.0);
        dto.setStats(stats);

        dto.setSocialLinks(null);

        return dto;
    }

    @Override
    public OrganizerDashboardDTO getOrganizerDashboard(int userId) {
        Organizer organizer = organizerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Organizer not found for user id: " + userId));

        int organizerId = organizer.getId();

        // Get the user for notifications
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Calculate stats
        OrganizerDashboardDTO.DashboardStats stats = new OrganizerDashboardDTO.DashboardStats();
        stats.setTotalEvents(eventRepository.countByOrganizerId(organizerId));
        stats.setTotalParticipants(eventRegistrationRepository.sumParticipantsByOrganizerId(organizerId));

        // Count new reviews (reviews created in last 7 days)
        Long totalReviews = reviewRepository.countByOrganizerId(organizerId);
        stats.setNewReviews(totalReviews != null ? totalReviews.intValue() : 0);

        // Calculate total earnings from successful payments
        Double totalEarnings = paymentRepository.sumTotalEarningsByOrganizerId(organizerId, PaymentStatus.SUCCESS);
        stats.setTotalEarnings(totalEarnings != null ? totalEarnings : 0.0);

        // Get upcoming events (limit to 3)
        List<Event> upcomingEventsList = eventRepository.findUpcomingEventsByOrganizerId(organizerId,
                EventStatus.ACTIVE);
        List<OrganizerDashboardDTO.UpcomingEventDTO> upcomingEvents = upcomingEventsList.stream()
                .limit(3)
                .map(this::mapToUpcomingEventDTO)
                .toList();

        // Get recent registrations (limit to 5)
        List<EventRegistration> recentRegistrationsList = eventRegistrationRepository
                .findRecentRegistrationsByOrganizerId(organizerId);
        List<OrganizerDashboardDTO.RecentRegistrationDTO> recentRegistrations = recentRegistrationsList.stream()
                .limit(5)
                .map(this::mapToRecentRegistrationDTO)
                .toList();

        // Get recent reviews (limit to 2)
        List<Reviews> recentReviewsList = reviewRepository.findRecentReviewsByOrganizerId(organizerId);
        List<OrganizerDashboardDTO.ReviewDTO> reviews = recentReviewsList.stream()
                .limit(2)
                .map(this::mapToReviewDTO)
                .toList();

        // Get notifications for the organizer's user (FIXED)
        Pageable limit = PageRequest.of(0, 3);
        List<NotificationRecipient> notificationsList = notificationRecipientRepository
                .findTop3ByUserIdOrderByCreatedAtDesc(userId, limit);

        List<OrganizerDashboardDTO.NotificationDTO> notifications = notificationsList.stream()
                .map(recipient -> mapToNotificationDTO(recipient.getNotification(), recipient.isRead()))
                .toList();

        OrganizerDashboardDTO dashboardDTO = new OrganizerDashboardDTO();
        dashboardDTO.setStats(stats);
        dashboardDTO.setUpcomingEvents(upcomingEvents);
        dashboardDTO.setRecentRegistrations(recentRegistrations);
        dashboardDTO.setReviews(reviews);
        dashboardDTO.setNotifications(notifications);

        return dashboardDTO;
    }

    private OrganizerDashboardDTO.UpcomingEventDTO mapToUpcomingEventDTO(Event event) {
        OrganizerDashboardDTO.UpcomingEventDTO dto = new OrganizerDashboardDTO.UpcomingEventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDate(event.getDate());
        dto.setTime(event.getMeetingTime());
        dto.setDifficulty(event.getDifficultyLevel() != null ? event.getDifficultyLevel().name() : "EASY");
        dto.setMaxParticipants(event.getMaxParticipants());
        dto.setImage(event.getBannerImageUrl());

        // Count participants
        int participantCount = event.getEventRegistration() != null
                ? event.getEventRegistration().stream()
                        .mapToInt(reg -> reg.getEventParticipants() != null ? reg.getEventParticipants().size() : 0)
                        .sum()
                : 0;
        dto.setParticipants(participantCount);

        return dto;
    }

    private OrganizerDashboardDTO.RecentRegistrationDTO mapToRecentRegistrationDTO(EventRegistration registration) {
        OrganizerDashboardDTO.RecentRegistrationDTO dto = new OrganizerDashboardDTO.RecentRegistrationDTO();
        dto.setId(registration.getId());
        dto.setName(registration.getContactName() != null ? registration.getContactName()
                : registration.getUser().getName());
        dto.setEvent(registration.getEvent().getTitle());
        dto.setRegistrationDate(registration.getRegistrationDate());
        dto.setContact(registration.getContact() != null ? registration.getContact() : registration.getEmail());
        dto.setStatus(registration.getStatus() != null ? registration.getStatus().name() : "PENDING");
        return dto;
    }

    private OrganizerDashboardDTO.ReviewDTO mapToReviewDTO(Reviews review) {
        OrganizerDashboardDTO.ReviewDTO dto = new OrganizerDashboardDTO.ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setAuthor(review.getUser().getName());
        dto.setDate(review.getCreatedAt());
        return dto;
    }

    private OrganizerDashboardDTO.NotificationDTO mapToNotificationDTO(Notification notification, boolean isRead) {
        OrganizerDashboardDTO.NotificationDTO dto = new OrganizerDashboardDTO.NotificationDTO();

        dto.setId(notification.getId());
        dto.setType(notification.getType() != null ? notification.getType().name() : "SYSTEM");
        dto.setTitle(notification.getTitle() != null ? notification.getTitle().trim() : "Notification");
        dto.setMessage(notification.getMessage() != null ? notification.getMessage() : "");
        dto.setTime(notification.getCreatedAt());
        dto.setRead(isRead);

        return dto;
    }
}
