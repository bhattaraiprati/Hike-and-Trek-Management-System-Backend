package com.example.treksathi.repository;

import com.example.treksathi.enums.*;
import com.example.treksathi.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PaymentRepository Integration Tests")
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    private Organizer organizer;
    private User user;
    private Event event;
    private EventRegistration eventRegistration;
    private Payments payment;

    @BeforeEach
    void setUp() {
        // Create and persist User
        user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole(Role.ORGANIZER);
        user = entityManager.persistAndFlush(user);

        // Create and persist Organizer
        organizer = new Organizer();
        organizer.setUser(user);
        organizer.setOrganization_name("Test Organization");
        organizer = entityManager.persistAndFlush(organizer);

        // Create and persist Event
        event = new Event();
        event.setTitle("Test Event");
        event.setDate(LocalDate.now().plusDays(10));
        event.setStatus(EventStatus.ACTIVE);
        event.setOrganizer(organizer);
        event.setPrice(100.0);
        event = entityManager.persistAndFlush(event);

        // Create and persist EventRegistration
        eventRegistration = new EventRegistration();
        eventRegistration.setEvent(event);
        eventRegistration.setUser(user);
        eventRegistration.setContactName("Test Contact");
        eventRegistration.setEmail("contact@example.com");
        eventRegistration.setStatus(EventRegistrationStatus.SUCCESS);
        eventRegistration = entityManager.persistAndFlush(eventRegistration);

        // Create and persist Payment
        payment = new Payments();
        payment.setEventRegistration(eventRegistration);
        payment.setAmount(200.0);
        payment.setMethod(PaymentMethod.CARD);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionUuid("TXN-123");
        payment.setTransactionDate(LocalDateTime.now());
        payment = entityManager.persistAndFlush(payment);
    }

    @Test
    @DisplayName("Should find payment by transaction UUID")
    void testFindByTransactionUuid() {
        // When
        Optional<Payments> found = paymentRepository.findByTransactionUuid("TXN-123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTransactionUuid()).isEqualTo("TXN-123");
        assertThat(found.get().getAmount()).isEqualTo(200.0);
    }

    @Test
    @DisplayName("Should return empty when transaction UUID not found")
    void testFindByTransactionUuid_NotFound() {
        // When
        Optional<Payments> found = paymentRepository.findByTransactionUuid("NON-EXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find payments by organizer ID")
    void testFindByOrganizerId() {
        // When
        List<Payments> payments = paymentRepository.findByOrganizerId(organizer.getId());

        // Then
        assertThat(payments).isNotEmpty();
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getId()).isEqualTo(payment.getId());
    }

    @Test
    @DisplayName("Should return empty list when organizer has no payments")
    void testFindByOrganizerId_NoPayments() {
        // Given - Create another organizer with no payments
        User user2 = new User();
        user2.setName("Another User");
        user2.setEmail("another@example.com");
        user2.setRole(Role.ORGANIZER);
        user2 = entityManager.persistAndFlush(user2);

        Organizer organizer2 = new Organizer();
        organizer2.setUser(user2);
        organizer2.setOrganization_name("Another Organization");
        organizer2 = entityManager.persistAndFlush(organizer2);

        // When
        List<Payments> payments = paymentRepository.findByOrganizerId(organizer2.getId());

        // Then
        assertThat(payments).isEmpty();
    }

    @Test
    @DisplayName("Should calculate total earnings by organizer ID and status")
    void testSumTotalEarningsByOrganizerId() {
        // Given - Create another successful payment
        EventRegistration reg2 = new EventRegistration();
        reg2.setEvent(event);
        reg2.setUser(user);
        reg2.setStatus(EventRegistrationStatus.SUCCESS);
        reg2 = entityManager.persistAndFlush(reg2);

        Payments payment2 = new Payments();
        payment2.setEventRegistration(reg2);
        payment2.setAmount(300.0);
        payment2.setMethod(PaymentMethod.ESEWA);
        payment2.setPaymentStatus(PaymentStatus.SUCCESS);
        payment2.setTransactionUuid("TXN-456");
        payment2.setTransactionDate(LocalDateTime.now());
        entityManager.persistAndFlush(payment2);

        // When
        Double totalEarnings = paymentRepository.sumTotalEarningsByOrganizerId(
                organizer.getId(), PaymentStatus.SUCCESS);

        // Then
        assertThat(totalEarnings).isNotNull();
        assertThat(totalEarnings).isEqualTo(500.0); // 200 + 300
    }

    @Test
    @DisplayName("Should return zero when no payments match status")
    void testSumTotalEarningsByOrganizerId_NoMatchingStatus() {
        // When
        Double totalEarnings = paymentRepository.sumTotalEarningsByOrganizerId(
                organizer.getId(), PaymentStatus.PENDING);

        // Then
        assertThat(totalEarnings).isNotNull();
        assertThat(totalEarnings).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should find payments with filters")
    void testFindByOrganizerIdWithFilters() {
        // Given - Create a pending payment
        EventRegistration reg2 = new EventRegistration();
        reg2.setEvent(event);
        reg2.setUser(user);
        reg2.setStatus(EventRegistrationStatus.SUCCESS);
        reg2 = entityManager.persistAndFlush(reg2);

        Payments pendingPayment = new Payments();
        pendingPayment.setEventRegistration(reg2);
        pendingPayment.setAmount(150.0);
        pendingPayment.setMethod(PaymentMethod.ESEWA);
        pendingPayment.setPaymentStatus(PaymentStatus.PENDING);
        pendingPayment.setTransactionUuid("TXN-789");
        pendingPayment.setTransactionDate(LocalDateTime.now());
        entityManager.persistAndFlush(pendingPayment);

        // When - Filter by SUCCESS status
        List<Payments> successPayments = paymentRepository.findByOrganizerIdWithFilters(
                organizer.getId(),
                PaymentStatus.SUCCESS,
                null,
                null,
                null,
                null
        );

        // Then
        assertThat(successPayments).isNotEmpty();
        assertThat(successPayments).hasSize(1);
        assertThat(successPayments.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    @DisplayName("Should filter payments by event ID")
    void testFindByOrganizerIdWithFilters_ByEventId() {
        // Given - Create another event with payment
        Event event2 = new Event();
        event2.setTitle("Another Event");
        event2.setDate(LocalDate.now().plusDays(20));
        event2.setStatus(EventStatus.ACTIVE);
        event2.setOrganizer(organizer);
        event2.setPrice(150.0);
        event2 = entityManager.persistAndFlush(event2);

        EventRegistration reg2 = new EventRegistration();
        reg2.setEvent(event2);
        reg2.setUser(user);
        reg2.setStatus(EventRegistrationStatus.SUCCESS);
        reg2 = entityManager.persistAndFlush(reg2);

        Payments payment2 = new Payments();
        payment2.setEventRegistration(reg2);
        payment2.setAmount(300.0);
        payment2.setMethod(PaymentMethod.CARD);
        payment2.setPaymentStatus(PaymentStatus.SUCCESS);
        payment2.setTransactionUuid("TXN-999");
        payment2.setTransactionDate(LocalDateTime.now());
        entityManager.persistAndFlush(payment2);

        // When - Filter by event ID
        List<Payments> filteredPayments = paymentRepository.findByOrganizerIdWithFilters(
                organizer.getId(),
                null,
                event.getId(),
                null,
                null,
                null
        );

        // Then
        assertThat(filteredPayments).isNotEmpty();
        assertThat(filteredPayments).allMatch(p -> 
                p.getEventRegistration().getEvent().getId() == event.getId());
    }

    @Test
    @DisplayName("Should filter payments by payment method")
    void testFindByOrganizerIdWithFilters_ByPaymentMethod() {
        // Given - Create payment with ESEWA method
        EventRegistration reg2 = new EventRegistration();
        reg2.setEvent(event);
        reg2.setUser(user);
        reg2.setStatus(EventRegistrationStatus.SUCCESS);
        reg2 = entityManager.persistAndFlush(reg2);

        Payments esewaPayment = new Payments();
        esewaPayment.setEventRegistration(reg2);
        esewaPayment.setAmount(250.0);
        esewaPayment.setMethod(PaymentMethod.ESEWA);
        esewaPayment.setPaymentStatus(PaymentStatus.SUCCESS);
        esewaPayment.setTransactionUuid("TXN-ESEWA");
        esewaPayment.setTransactionDate(LocalDateTime.now());
        entityManager.persistAndFlush(esewaPayment);

        // When - Filter by CARD method
        List<Payments> cardPayments = paymentRepository.findByOrganizerIdWithFilters(
                organizer.getId(),
                null,
                null,
                PaymentMethod.CARD,
                null,
                null
        );

        // Then
        assertThat(cardPayments).isNotEmpty();
        assertThat(cardPayments).allMatch(p -> p.getMethod() == PaymentMethod.CARD);
    }

    @Test
    @DisplayName("Should filter payments by date range")
    void testFindByOrganizerIdWithFilters_ByDateRange() {
        // Given - Create payment with different date
        EventRegistration reg2 = new EventRegistration();
        reg2.setEvent(event);
        reg2.setUser(user);
        reg2.setStatus(EventRegistrationStatus.SUCCESS);
        reg2 = entityManager.persistAndFlush(reg2);

        Payments oldPayment = new Payments();
        oldPayment.setEventRegistration(reg2);
        oldPayment.setAmount(100.0);
        oldPayment.setMethod(PaymentMethod.CARD);
        oldPayment.setPaymentStatus(PaymentStatus.SUCCESS);
        oldPayment.setTransactionUuid("TXN-OLD");
        oldPayment.setTransactionDate(LocalDateTime.now().minusMonths(2));
        entityManager.persistAndFlush(oldPayment);

        // When - Filter by date range (last month)
        LocalDateTime fromDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime toDate = LocalDateTime.now().plusDays(1);

        List<Payments> recentPayments = paymentRepository.findByOrganizerIdWithFilters(
                organizer.getId(),
                null,
                null,
                null,
                fromDate,
                toDate
        );

        // Then
        assertThat(recentPayments).isNotEmpty();
        assertThat(recentPayments).allMatch(p -> 
                p.getTransactionDate().isAfter(fromDate) && 
                p.getTransactionDate().isBefore(toDate));
    }
}

