package com.example.treksathi.repository;

import com.example.treksathi.model.ChatRoom;
import com.example.treksathi.model.Event;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {

    // Find chat room by associated Event (for event group chats)
    Optional<ChatRoom> findByEvent(Event event);

    // Find all chat rooms (channels) belonging to an Organizer
    List<ChatRoom> findByOrganizer(Organizer organizer);


    boolean existsByIdAndParticipantsContaining(int id, User user);
    Optional<ChatRoom> findByOrganizerAndEvent(Organizer organizer, Event event);

    // Find all chat rooms a User is participating in
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.participants p WHERE p = :user")
    List<ChatRoom> findByParticipantsContaining(@Param("user") User user);

    // Check if a user is a participant in a specific chat room (used for authorization)
    boolean existsByIdAndParticipantsContaining(Long chatRoomId, User user);

    // Optional: Find organizer's main channel (e.g., named "general")
    Optional<ChatRoom> findByOrganizerAndName(Organizer organizer, String name);
}