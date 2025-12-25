package com.example.treksathi.service;

import com.example.treksathi.enums.NotificationType;
import com.example.treksathi.model.Notification;
import com.example.treksathi.model.User;
import com.example.treksathi.record.CreateNotificationRequest;
import com.example.treksathi.record.NotificationResponseDTO;
import com.example.treksathi.repository.NotificationRepository;
import com.example.treksathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private  final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationResponseDTO createAndSendNotification(int userId, CreateNotificationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setType(NotificationType.valueOf(request.type()));
        notification.setRead(false);

        notification = notificationRepository.save(notification);

        NotificationResponseDTO dto = mapToDTO(notification);

        // Send real-time notification via WebSocket
        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/notifications",
                dto
        );

        return dto;
    }

    @Transactional
    public void broadcastNotification(List<Integer> userIds, CreateNotificationRequest request) {
        List<User> users = userRepository.findAllById(userIds);

        users.forEach(user -> {
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle(request.title());
            notification.setMessage(request.message());
            notification.setType(NotificationType.valueOf(request.type()));

            notificationRepository.save(notification);

            NotificationResponseDTO dto = mapToDTO(notification);
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/queue/notifications",
                    dto
            );
        });
    }

    public Page<NotificationResponseDTO> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user, pageable)
                .map(this::mapToDTO);
    }

    public int getUnreadCount(User user) {
        return notificationRepository.countUnreadByRecipientId(user.getId());
    }

    @Transactional
    public void markAsRead(int notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.getRecipient().getId() != user.getId()) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsRead(User user) {
        return notificationRepository.markAllAsReadForUser(user);
    }

    @Transactional
    public void deleteNotification(int notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.getRecipient().getId() != user.getId()) {
            throw new RuntimeException("Unauthorized");
        }

        notificationRepository.delete(notification);
    }


    private NotificationResponseDTO mapToDTO(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getType(),
                notification.getCreatedAt().toString()
        );
    }
}
