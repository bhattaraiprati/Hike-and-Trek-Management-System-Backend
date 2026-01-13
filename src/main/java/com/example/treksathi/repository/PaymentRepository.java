package com.example.treksathi.repository;

import com.example.treksathi.enums.PaymentMethod;
import com.example.treksathi.enums.PaymentStatus;
import com.example.treksathi.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payments,Integer> {

    Optional<Payments> findByTransactionUuid(String id);

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payments p " +
            "JOIN p.eventRegistration er " +
            "JOIN er.event e " +
            "WHERE e.organizer.id = :organizerId AND p.paymentStatus = :status")
    Double sumTotalEarningsByOrganizerId(@Param("organizerId") int organizerId, @Param("status") PaymentStatus status);

    List<Payments> findTop5ByEventRegistrationUserIdOrderByTransactionDateDesc(int userId);

    @Query("SELECT p FROM Payments p " +
            "JOIN FETCH p.eventRegistration er " +
            "JOIN FETCH er.event e " +
            "JOIN FETCH er.user u " +
            "WHERE e.organizer.id = :organizerId")
    List<Payments> findByOrganizerId(@Param("organizerId") int organizerId);


    @Query("""
        SELECT p FROM Payments p
        JOIN p.eventRegistration er
        JOIN er.event e
        JOIN er.user u
        WHERE e.organizer.id = :organizerId
          AND (:status IS NULL OR p.paymentStatus = :status)
          AND (:eventId IS NULL OR e.id = :eventId)
          AND (:paymentMethod IS NULL OR p.method = :paymentMethod)
          AND (:fromDate IS NULL OR p.transactionDate >= :fromDate)
          AND (:toDate   IS NULL OR p.transactionDate <= :toDate)
    """)
    List<Payments> findByOrganizerIdWithFilters(
            @Param("organizerId") Integer organizerId,
            @Param("status") PaymentStatus status,
            @Param("eventId") Integer eventId,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
