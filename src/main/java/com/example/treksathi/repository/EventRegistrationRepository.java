package com.example.treksathi.repository;

import com.example.treksathi.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Integer> {

    @Query("SELECT COUNT(ep) FROM EventRegistration er " +
            "JOIN er.event e " +
            "JOIN er.eventParticipants ep " +
            "WHERE e.organizer.id = :organizerId")
    int sumParticipantsByOrganizerId(@Param("organizerId") int organizerId);

    Optional<List<EventRegistration>> findByUserId(int id);
}
