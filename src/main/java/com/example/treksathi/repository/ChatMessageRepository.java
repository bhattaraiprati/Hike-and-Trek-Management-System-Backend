package com.example.treksathi.repository;

import com.example.treksathi.model.ChatMessage;
import com.example.treksathi.model.ChatRoom;
import com.example.treksathi.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Get messages for a chat room, ordered by timestamp (newest first)
    List<ChatMessage> findByChatRoomOrderByTimestampDesc(ChatRoom chatRoom, Pageable pageable);

    // Get all messages in a room (for loading full history)
    List<ChatMessage> findByChatRoomOrderByTimestampAsc(ChatRoom chatRoom);

    // Get messages after a specific timestamp (for real-time updates / pagination)
    List<ChatMessage> findByChatRoomAndTimestampAfterOrderByTimestampAsc(
            ChatRoom chatRoom, LocalDateTime timestamp);

    // Get the latest message in a room (useful for previews)
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = :chatRoom ORDER BY m.timestamp DESC")
    List<ChatMessage> findTopByChatRoomOrderByTimestampDesc(@Param("chatRoom") ChatRoom chatRoom, Pageable pageable);

    // Count unread messages for a user in a room (optional advanced feature)
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.chatRoom = :chatRoom " +
            "AND m.timestamp > (" +
            "   SELECT COALESCE(MAX(rr.readAt), '1970-01-01') " +
            "   FROM ReadReceipt rr " +
            "   WHERE rr.message.chatRoom = :chatRoom AND rr.user = :user" +
            ")")
    long countUnreadMessages(@Param("chatRoom") ChatRoom chatRoom, @Param("user") User user);
}