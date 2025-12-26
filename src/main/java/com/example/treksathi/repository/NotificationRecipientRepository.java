package com.example.treksathi.repository;

import com.example.treksathi.model.NotificationRecipient;
import com.example.treksathi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Integer> {

    Page<NotificationRecipient> findByUserOrderByNotification_CreatedAtDesc(User user, Pageable pageable);

    @Query("SELECT COUNT(nr) FROM NotificationRecipient nr WHERE nr.user = :user AND nr.isRead = false")
    int countUnreadByUser(@Param("user") User user);

    Optional<NotificationRecipient> findByNotificationIdAndUser(int notificationId, User user);

    @Query("SELECT nr FROM NotificationRecipient nr " +
            "JOIN FETCH nr.notification n " +
            "WHERE nr.user.id = :userId " +
            "ORDER BY n.createdAt DESC")
    List<NotificationRecipient> findTop3ByUserIdOrderByCreatedAtDesc(@Param("userId") int userId, Pageable pageable);
    @Modifying
    @Query("UPDATE NotificationRecipient nr SET nr.isRead = true, nr.readAt = CURRENT_TIMESTAMP WHERE nr.user = :user AND nr.isRead = false")
    int markAllAsReadForUser(@Param("user") User user);

    @Query("SELECT nr FROM NotificationRecipient nr JOIN FETCH nr.notification WHERE nr.user = :user ORDER BY nr.notification.createdAt DESC")
    Page<NotificationRecipient> findByUserWithNotification(@Param("user") User user, Pageable pageable);
}