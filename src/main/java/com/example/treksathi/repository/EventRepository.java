package com.example.treksathi.repository;

import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.Event;
import com.example.treksathi.model.Organizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {

    List<Event> findByOrganizer(Organizer organizer);

    @Query("SELECT e from Event e"+
    " where e.status='ACTIVE'")
    Page<Event> findByAll(Pageable pageable);

    Page<Event> findByStatus(@Param("status") EventStatus status, Pageable pageable);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByDifficultyLevel(String difficultyLevel);

    List<Event> findByLocationContainingIgnoreCase(String location);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizer.id = :organizerId")
    int countByOrganizerId(@Param("organizerId") int organizerId);
}
