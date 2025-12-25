package com.example.treksathi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.treksathi.model.Notification;
import com.example.treksathi.model.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(@Param("userId") int userId);
    List<Notification> findByRecipientAndReadIsFalseOrderByCreatedAtDesc(User user);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = false")
    int countUnreadByRecipientId(@Param("userId") int userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :user AND n.isRead = false")
    int markAllAsReadForUser(User user);
}

