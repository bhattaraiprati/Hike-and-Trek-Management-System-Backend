package com.example.treksathi.controller;

import com.example.treksathi.model.User;
import com.example.treksathi.record.CreateNotificationRequest;
import com.example.treksathi.record.NotificationResponseDTO;
import com.example.treksathi.repository.UserRepository;
import com.example.treksathi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getNotifications(
            Authentication authentication,
            Pageable pageable) {
        User user = getUserFromAuth(authentication);
        return ResponseEntity.ok(notificationService.getUserNotifications(user, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        Integer count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable int id,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        int count = notificationService.markAllAsRead(user);
        return ResponseEntity.ok(Map.of("markedCount", count));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable int id,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        notificationService.deleteNotification(id, user);
        return ResponseEntity.ok().build();
    }

    // Admin endpoint to send notifications
    @PostMapping("/send/{userId}")
    public ResponseEntity<NotificationResponseDTO> sendNotification(
            @PathVariable int userId,
            @RequestBody CreateNotificationRequest request) {
        NotificationResponseDTO notification = notificationService.createAndSendNotification(userId, request);
        return ResponseEntity.ok(notification);
    }

    private User getUserFromAuth(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
