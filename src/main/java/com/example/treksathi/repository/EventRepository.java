package com.example.treksathi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.Event;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.record.EventCardResponse;

public interface EventRepository extends JpaRepository<Event, Integer> {

    @Query("SELECT e FROM Event e WHERE e.organizer = :organizer AND e.status <> com.example.treksathi.enums.EventStatus.DELETED")
    List<Event> findByOrganizer(@Param("organizer") Organizer organizer);

    @Query("SELECT e from Event e"+
    " where e.status='ACTIVE'")
    Page<Event> findByAll(Pageable pageable);

    Page<Event> findByStatus(@Param("status") EventStatus status, Pageable pageable);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByDifficultyLevel(String difficultyLevel);

    List<Event> findByLocationContainingIgnoreCase(String location);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizer.id = :organizerId")
    int countByOrganizerId(@Param("organizerId") int organizerId);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizer.id = :organizerId AND e.status = :status")
    int countByOrganizerIdAndStatus(@Param("organizerId") int organizerId, @Param("status") EventStatus status);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizer.id = :organizerId AND e.status = :status AND e.date >= CURRENT_DATE")
    int countUpcomingEventsByOrganizerIdAndStatus(@Param("organizerId") int organizerId, @Param("status") EventStatus status);
    @Query("SELECT e FROM Event e WHERE e.organizer.id = :organizerId AND e.status = :status AND e.date >= CURRENT_DATE ORDER BY e.date ASC")
    List<Event> findUpcomingEventsByOrganizerId(@Param("organizerId") int organizerId, @Param("status") EventStatus status);
    @Query("SELECT new com.example.treksathi.record.EventCardResponse(" +
            "e.id, e.title, e.description, e.location, e.date, " +
            "e.durationDays, " +
            "CAST(e.difficultyLevel AS string), " +
            "e.price, e.maxParticipants, e.bannerImageUrl, " +
            "CAST(e.status AS string), " +
            "COUNT(DISTINCT ep.id)) " +
            "FROM Event e " +
            "LEFT JOIN e.eventRegistration er " +
            "LEFT JOIN er.eventParticipants ep " +
            "WHERE e.status = :status " +
            "GROUP BY e.id, e.title, e.description, e.location, e.date, " +
            "e.durationDays, e.difficultyLevel, e.price, e.maxParticipants, " +
            "e.bannerImageUrl, e.status"+
            " order by e.date desc ")
    Page<EventCardResponse> findEventCardsWithParticipantCount(
            @Param("status") EventStatus status,
            Pageable pageable
    );
}
