package com.example.treksathi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.treksathi.model.Reviews;

public interface ReviewRepository extends JpaRepository<Reviews, Integer> {

    @Query("SELECT AVG(r.rating) FROM Reviews r " +
            "JOIN r.events e " +
            "WHERE e.organizer.id = :organizerId")
    Double findAverageRatingByOrganizerId(@Param("organizerId") int organizerId);

    @Query("SELECT COUNT(r) FROM Reviews r " +
            "JOIN r.events e " +
            "WHERE e.organizer.id = :organizerId")
    Long countByOrganizerId(@Param("organizerId") int organizerId);

    @Query("SELECT r FROM Reviews r " +
            "JOIN FETCH r.events e " +
            "JOIN FETCH r.user u " +
            "WHERE e.organizer.id = :organizerId " +
            "ORDER BY r.createdAt DESC")
    List<Reviews> findRecentReviewsByOrganizerId(@Param("organizerId") int organizerId);

    List<Reviews> findTop5ByUserIdOrderByCreatedAtDesc(int userId);

    @Query("SELECT AVG(r.rating) FROM Reviews r WHERE r.events.id = :eventId")
    Double findAverageRatingByEventId(@Param("eventId") int eventId);

    int countByEventsId(int eventId);
    int countByUserIdAndEventsId(int userId, int eventId);
}
