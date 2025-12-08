package com.example.treksathi.repository;

import com.example.treksathi.enums.EventRegistrationStatus;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.record.EventCardResponse;
import com.example.treksathi.record.UpcommingEventRecord;
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

        @Query("SELECT er FROM EventRegistration er " +
                " join er.event e"+
                " WHERE er.user.id = :id AND er.status IN :status")
        Optional<List<EventRegistration>> findByUserIdAndStatus(@Param("id") int id, @Param("status") List<EventRegistrationStatus> status);

        // src/main/java/com/example/treksathi/repository/EventRegistrationRepository.java
        @Query("SELECT new com.example.treksathi.record.UpcommingEventRecord(" +
                "er.id, " +
                "e.title, " +
                "CAST(e.date AS string), " +
                "e.location, " +
                "e.organizer.organization_name, " +
                "CAST(e.meetingTime AS string), " +
                "SIZE(er.eventParticipants), " +
                "CAST(e.status AS string), " +
                "e.bannerImageUrl) " +
                "FROM EventRegistration er " +
                "JOIN er.event e " +
                "WHERE er.user.id = :userId AND e.status = :status")
        Optional<List<UpcommingEventRecord>> findActiveEventByUserId(@Param("userId") int userId, @Param("status") EventStatus status);
     }
