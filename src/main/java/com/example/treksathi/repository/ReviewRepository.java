package com.example.treksathi.repository;

import com.example.treksathi.model.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Reviews, Integer> {

    @Query("SELECT AVG(r.rating) FROM Reviews r " +
            "JOIN r.events e " +
            "WHERE e.organizer.id = :organizerId")
    Double findAverageRatingByOrganizerId(@Param("organizerId") int organizerId);

    @Query("SELECT COUNT(r) FROM Reviews r " +
            "JOIN r.events e " +
            "WHERE e.organizer.id = :organizerId")
    Long countByOrganizerId(@Param("organizerId") int organizerId);
}
