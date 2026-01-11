package com.example.treksathi.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.treksathi.enums.EventRegistrationStatus;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.model.User;
import com.example.treksathi.record.UpcommingEventRecord;

    public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Integer> {

        @Query("SELECT COUNT(ep) FROM EventRegistration er " +
                "JOIN er.event e " +
                "JOIN er.eventParticipants ep " +
                "WHERE e.organizer.id = :organizerId")
        int sumParticipantsByOrganizerId(@Param("organizerId") int organizerId);

        EventRegistration findByUser(User user);

        Optional<List<EventRegistration>> findByUserId(int id);

        @Query("SELECT er FROM EventRegistration er " +
                "LEFT JOIN FETCH er.payments " +
                "LEFT JOIN FETCH er.event e " +
                "LEFT JOIN FETCH e.organizer " +
                "WHERE er.user.id = :userId AND er.status IN :statuses")
        Optional<List<EventRegistration>> findByUserIdAndStatusWithPayments(
                @Param("userId") int userId,
                @Param("statuses") List<EventRegistrationStatus> statuses
        );
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

        @Query("SELECT er FROM EventRegistration er " +
                "JOIN FETCH er.event e " +
                "JOIN FETCH er.user u " +
                "WHERE e.organizer.id = :organizerId " +
                "ORDER BY er.registrationDate DESC")
        List<EventRegistration> findRecentRegistrationsByOrganizerId(@Param("organizerId") int organizerId);

        List<EventRegistration> findByUserIdOrderByRegistrationDateDesc(int userId);
        List<EventRegistration> findByUserIdAndEventDateAfterOrderByEventDateAsc(int userId, LocalDate date);
        List<EventRegistration> findTop5ByUserIdOrderByRegistrationDateDesc(int userId);
        int countByUserIdAndEventDateBeforeAndStatus(int userId, LocalDate date, EventRegistrationStatus status);
        int countByEventId(int eventId);
        List<EventRegistration> findByUserIdAndEventStatus(int userId, EventStatus status);
     }
