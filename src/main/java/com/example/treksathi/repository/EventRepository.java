package com.example.treksathi.repository;

import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.Event;
import com.example.treksathi.model.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {

    List<Event> findByOrganizer(Organizer organizer);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByDifficultyLevel(String difficultyLevel);

    List<Event> findByLocationContainingIgnoreCase(String location);
}
