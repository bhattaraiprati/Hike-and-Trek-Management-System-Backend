package com.example.treksathi.service;

import com.example.treksathi.enums.NotificationType;
import com.example.treksathi.model.Notification;
import com.example.treksathi.model.NotificationRecipient;
import com.example.treksathi.model.User;
import com.example.treksathi.record.CreateNotificationRequest;
import com.example.treksathi.record.NotificationResponseDTO;
import com.example.treksathi.repository.NotificationRecipientRepository;
import com.example.treksathi.repository.NotificationRepository;
import com.example.treksathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository recipientRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send notification to a single user
     */
    @Transactional
    public NotificationResponseDTO createAndSendNotification(int userId, CreateNotificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = createNotification(request);
        notification.addRecipient(user);

        notification = notificationRepository.save(notification);

        NotificationResponseDTO dto = mapToDTO(notification, user);

        // Send real-time notification via WebSocket
        sendWebSocketNotification(user.getEmail(), dto);

        return dto;
    }

    /**
     * Broadcast notification to multiple users (e.g., event participants)
     */
    @Transactional
    public void broadcastNotification(List<Integer> userIds, CreateNotificationRequest request) {
        List<User> users = userRepository.findAllById(userIds);

        if (users.isEmpty()) {
            log.warn("No users found for notification broadcast");
            return;
        }

        Notification notification = createNotification(request);
        notification.addRecipients(users);

        notification = notificationRepository.save(notification);

        // Send WebSocket notification to each user
        for (User user : users) {
            NotificationResponseDTO dto = mapToDTO(notification, user);
            sendWebSocketNotification(user.getEmail(), dto);
        }

        log.info("Broadcast notification sent to {} users", users.size());
    }

    /**
     * Get notifications for a specific user
     */
    public Page<NotificationResponseDTO> getUserNotifications(User user, Pageable pageable) {
        return recipientRepository.findByUserWithNotification(user, pageable)
                .map(recipient -> mapToDTO(recipient.getNotification(), user));
    }

    /**
     * Get unread notification count
     */
    public int getUnreadCount(User user) {
        return recipientRepository.countUnreadByUser(user);
    }

    /**
     * Mark a specific notification as read
     */
    @Transactional
    public void markAsRead(int notificationId, User user) {
        NotificationRecipient recipient = recipientRepository
                .findByNotificationIdAndUser(notificationId, user)
                .orElseThrow(() -> new RuntimeException("Notification not found for this user"));

        if (!recipient.isRead()) {
            recipient.setRead(true);
            recipientRepository.save(recipient);
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public int markAllAsRead(User user) {
        return recipientRepository.markAllAsReadForUser(user);
    }

    /**
     * Delete a notification for a specific user
     */
    @Transactional
    public void deleteNotification(int notificationId, User user) {
        NotificationRecipient recipient = recipientRepository
                .findByNotificationIdAndUser(notificationId, user)
                .orElseThrow(() -> new RuntimeException("Notification not found for this user"));

        recipientRepository.delete(recipient);
    }

    /**
     * Helper method to create notification entity
     */
    private Notification createNotification(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setType(NotificationType.valueOf(request.type()));
        notification.setReferenceId(request.referenceId());
        notification.setReferenceType(request.referenceType());
        return notification;
    }

    /**
     * Helper method to send WebSocket notification
     */
    private void sendWebSocketNotification(String userEmail, NotificationResponseDTO dto) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userEmail,
                    "/queue/notifications",
                    dto
            );
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user: {}", userEmail, e);
        }
    }

    /**
     * Map notification to DTO for specific user
     */
    private NotificationResponseDTO mapToDTO(Notification notification, User user) {
        // Find the recipient record for this user
        boolean isRead = notification.getRecipients().stream()
                .filter(r -> r.getUser().getId() == user.getId())
                .findFirst()
                .map(NotificationRecipient::isRead)
                .orElse(false);

        return new NotificationResponseDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                isRead,
                notification.getType(),
                notification.getCreatedAt().toString(),
                notification.getReferenceId(),
                notification.getReferenceType()
        );
    }
}