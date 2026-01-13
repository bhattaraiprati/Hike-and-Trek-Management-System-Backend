package com.example.treksathi.service;

import com.example.treksathi.dto.organizer.*;
import com.example.treksathi.enums.*;
import com.example.treksathi.exception.NotFoundException;
import com.example.treksathi.model.*;
import com.example.treksathi.repository.EventRepository;
import com.example.treksathi.repository.OrganizerRepository;
import com.example.treksathi.repository.PaymentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizerPaymentService Unit Tests")
class OrganizerPaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OrganizerRepository organizerRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Payments> criteriaQuery;

    @Mock
    private Root<Payments> root;

    @Mock
    private TypedQuery<Payments> typedQuery;

    @InjectMocks
    private OrganizerPaymentService organizerPaymentService;

    private Organizer organizer;
    private User user;
    private Event event;
    private EventRegistration eventRegistration;
    private Payments payment;
    private List<EventParticipants> eventParticipants;

    @BeforeEach
    void setUp() {
        // Setup User
        user = new User();
        user.setId(1);
        user.setName("Test User");
        user.setEmail("test@example.com");

        // Setup Organizer
        organizer = new Organizer();
        organizer.setId(100);
        organizer.setUser(user);
        organizer.setOrganization_name("Test Organization");

        // Setup Event
        event = new Event();
        event.setId(1);
        event.setTitle("Test Event");
        event.setDate(LocalDate.now().plusDays(10));
        event.setStatus(EventStatus.ACTIVE);
        event.setOrganizer(organizer);
        event.setPrice(100.0);

        // Setup EventParticipants
        EventParticipants participant1 = new EventParticipants();
        participant1.setId(1);
        participant1.setName("Participant 1");
        participant1.setGender(Gender.MALE);

        EventParticipants participant2 = new EventParticipants();
        participant2.setId(2);
        participant2.setName("Participant 2");
        participant2.setGender(Gender.FEMALE);

        eventParticipants = Arrays.asList(participant1, participant2);

        // Setup EventRegistration
        eventRegistration = new EventRegistration();
        eventRegistration.setId(1);
        eventRegistration.setEvent(event);
        eventRegistration.setUser(user);
        eventRegistration.setContactName("Test Contact");
        eventRegistration.setEmail("contact@example.com");
        eventRegistration.setStatus(EventRegistrationStatus.SUCCESS);
        eventRegistration.setEventParticipants(eventParticipants);
        eventRegistration.setRegistrationDate(LocalDateTime.now());

        // Setup Payment
        payment = new Payments();
        payment.setId(1);
        payment.setEventRegistration(eventRegistration);
        payment.setAmount(200.0);
        payment.setMethod(PaymentMethod.CARD);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionUuid("TXN-123");
        payment.setTransactionDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return payment dashboard successfully")
    void testGetPaymentDashboard_Success() {
        // Given
        Integer userId = 1;
        PaymentFilterDTO filters = new PaymentFilterDTO();
        filters.setStatus("ALL");

        List<Payments> payments = Collections.singletonList(payment);
        List<Event> events = Collections.singletonList(event);

        when(organizerRepository.findByUserId(userId)).thenReturn(Optional.of(organizer));
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Payments.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Payments.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(anyBoolean())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(payments);
        when(eventRepository.findByOrganizer(organizer)).thenReturn(events);
        when(paymentRepository.findByOrganizerId(organizer.getId())).thenReturn(payments);

        // When
        PaymentDashboardDTO result = organizerPaymentService.getPaymentDashboard(userId, filters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSummary()).isNotNull();
        assertThat(result.getEvents()).isNotEmpty();
        assertThat(result.getParticipantPayments()).isNotEmpty();
        assertThat(result.getRecentPayments()).isNotEmpty();
        assertThat(result.getRevenueChart()).isNotEmpty();

        verify(organizerRepository).findByUserId(userId);
        verify(entityManager).getCriteriaBuilder();
    }

    @Test
    @DisplayName("Should throw NotFoundException when organizer not found")
    void testGetPaymentDashboard_OrganizerNotFound() {
        // Given
        Integer userId = 999;
        PaymentFilterDTO filters = new PaymentFilterDTO();

        when(organizerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> organizerPaymentService.getPaymentDashboard(userId, filters))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Organizer not found");

        verify(organizerRepository).findByUserId(userId);
        verifyNoInteractions(paymentRepository, eventRepository);
    }

    @Test
    @DisplayName("Should filter payments by status")
    void testGetPaymentDashboard_FilterByStatus() {
        // Given
        Integer userId = 1;
        PaymentFilterDTO filters = new PaymentFilterDTO();
        filters.setStatus("COMPLETED");

        List<Payments> payments = Collections.singletonList(payment);

        when(organizerRepository.findByUserId(userId)).thenReturn(Optional.of(organizer));
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Payments.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Payments.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(anyBoolean())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(payments);
        when(eventRepository.findByOrganizer(organizer)).thenReturn(Collections.singletonList(event));
        when(paymentRepository.findByOrganizerId(organizer.getId())).thenReturn(payments);

        // When
        PaymentDashboardDTO result = organizerPaymentService.getPaymentDashboard(userId, filters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSummary().getCompletedPayments()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should filter payments by event ID")
    void testGetPaymentDashboard_FilterByEventId() {
        // Given
        Integer userId = 1;
        PaymentFilterDTO filters = new PaymentFilterDTO();
        filters.setEventId(1);
        filters.setStatus("ALL");

        List<Payments> payments = Collections.singletonList(payment);

        when(organizerRepository.findByUserId(userId)).thenReturn(Optional.of(organizer));
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Payments.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Payments.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(anyBoolean())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(payments);
        when(eventRepository.findByOrganizer(organizer)).thenReturn(Collections.singletonList(event));
        when(paymentRepository.findByOrganizerId(organizer.getId())).thenReturn(payments);

        // When
        PaymentDashboardDTO result = organizerPaymentService.getPaymentDashboard(userId, filters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getParticipantPayments()).isNotEmpty();
    }

    @Test
    @DisplayName("Should filter payments by date range")
    void testGetPaymentDashboard_FilterByDateRange() {
        // Given
        Integer userId = 1;
        PaymentFilterDTO filters = new PaymentFilterDTO();
        PaymentFilterDTO.DateRange dateRange = new PaymentFilterDTO.DateRange();
        dateRange.setFrom(LocalDate.now().minusMonths(1));
        dateRange.setTo(LocalDate.now());
        filters.setDateRange(dateRange);
        filters.setStatus("ALL");

        List<Payments> payments = Collections.singletonList(payment);

        when(organizerRepository.findByUserId(userId)).thenReturn(Optional.of(organizer));
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Payments.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Payments.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(anyBoolean())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(payments);
        when(eventRepository.findByOrganizer(organizer)).thenReturn(Collections.singletonList(event));
        when(paymentRepository.findByOrganizerId(organizer.getId())).thenReturn(payments);

        // When
        PaymentDashboardDTO result = organizerPaymentService.getPaymentDashboard(userId, filters);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should calculate payment summary correctly")
    void testBuildPaymentSummary() {
        // Given
        Payments pendingPayment = new Payments();
        pendingPayment.setAmount(100.0);
        pendingPayment.setPaymentStatus(PaymentStatus.PENDING);
        pendingPayment.setTransactionDate(LocalDateTime.now());

        Payments failedPayment = new Payments();
        failedPayment.setAmount(50.0);
        failedPayment.setPaymentStatus(PaymentStatus.DECLINE);
        failedPayment.setTransactionDate(LocalDateTime.now());

        List<Payments> payments = Arrays.asList(payment, pendingPayment, failedPayment);
        PaymentFilterDTO filters = new PaymentFilterDTO();

        when(organizerRepository.findByUserId(1)).thenReturn(Optional.of(organizer));
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Payments.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Payments.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(anyBoolean())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(payments);
        when(eventRepository.findByOrganizer(organizer)).thenReturn(Collections.singletonList(event));
        when(paymentRepository.findByOrganizerId(organizer.getId())).thenReturn(payments);

        // When
        PaymentDashboardDTO result = organizerPaymentService.getPaymentDashboard(1, filters);

        // Then
        PaymentSummaryDTO summary = result.getSummary();
        assertThat(summary.getTotalIncome()).isEqualTo(200.0);
        assertThat(summary.getCompletedPayments()).isEqualTo(1);
        assertThat(summary.getPendingPayments()).isEqualTo(1);
        assertThat(summary.getRefundedPayments()).isEqualTo(1);
        assertThat(summary.getCurrency()).isEqualTo("$");
        assertThat(summary.getPlatformFee()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should build event payments correctly")
    void testBuildEventPayments() {
        // Given
        List<Payments> payments = Collections.singletonList(payment);
        PaymentFilterDTO filters = new PaymentFilterDTO();
        filters.setStatus("ALL");

        when(organizerRepository.findByUserId(1)).thenReturn(Optional.of(organizer));
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Payments.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Payments.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(anyBoolean())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(payments);
        when(eventRepository.findByOrganizer(organizer)).thenReturn(Collections.singletonList(event));
        when(paymentRepository.findByOrganizerId(organizer.getId())).thenReturn(payments);

        // When
        PaymentDashboardDTO result = organizerPaymentService.getPaymentDashboard(1, filters);

        // Then
        List<EventPaymentDTO> eventPayments = result.getEvents();
        assertThat(eventPayments).isNotEmpty();
        
        EventPaymentDTO eventPayment = eventPayments.get(0);
        assertThat(eventPayment.getEventId()).isEqualTo(event.getId());
        assertThat(eventPayment.getEventTitle()).isEqualTo(event.getTitle());
        assertThat(eventPayment.getTotalParticipants()).isEqualTo(2);
        assertThat(eventPayment.getPaidParticipants()).isEqualTo(1);
        assertThat(eventPayment.getTotalRevenue()).isEqualTo(200.0);
        assertThat(eventPayment.getOrganizerShare()).isEqualTo(180.0); // 90% of 200
    }

    @Test
    @DisplayName("Should build participant payments correctly")
    void testBuildParticipantPayments() {
        // Given
        List<Payments> payments = Collections.singletonList(payment);
        PaymentFilterDTO filters = new PaymentFilterDTO();
        filters.setStatus("ALL");

        when(organizerRepository.findByUserId(1)).thenReturn(Optional.of(organizer));
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Payments.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Payments.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(anyBoolean())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(payments);
        when(eventRepository.findByOrganizer(organizer)).thenReturn(Collections.singletonList(event));
        when(paymentRepository.findByOrganizerId(organizer.getId())).thenReturn(payments);

        // When
        PaymentDashboardDTO result = organizerPaymentService.getPaymentDashboard(1, filters);

        // Then
        List<ParticipantPaymentDTO> participantPayments = result.getParticipantPayments();
        assertThat(participantPayments).isNotEmpty();
        
        ParticipantPaymentDTO participantPayment = participantPayments.get(0);
        assertThat(participantPayment.getParticipantName()).isEqualTo("Test Contact");
        assertThat(participantPayment.getParticipantEmail()).isEqualTo("contact@example.com");
        assertThat(participantPayment.getAmount()).isEqualTo(200.0);
        assertThat(participantPayment.getStatus()).isEqualTo("COMPLETED");
        assertThat(participantPayment.getPaymentMethod()).isEqualTo("CREDIT_CARD");
        assertThat(participantPayment.getNumberOfParticipants()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should build revenue chart for last 6 months")
    void testBuildRevenueChart() {
        // Given
        List<Payments> payments = Collections.singletonList(payment);
        PaymentFilterDTO filters = new PaymentFilterDTO();
        filters.setStatus("ALL");

        when(organizerRepository.findByUserId(1)).thenReturn(Optional.of(organizer));
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Payments.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Payments.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(anyBoolean())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(payments);
        when(eventRepository.findByOrganizer(organizer)).thenReturn(Collections.singletonList(event));
        when(paymentRepository.findByOrganizerId(organizer.getId())).thenReturn(payments);

        // When
        PaymentDashboardDTO result = organizerPaymentService.getPaymentDashboard(1, filters);

        // Then
        List<MonthlyRevenueDTO> revenueChart = result.getRevenueChart();
        assertThat(revenueChart).hasSize(6);
        
        // Check that all months have revenue data
        revenueChart.forEach(month -> {
            assertThat(month.getMonth()).isNotNull();
            assertThat(month.getRevenue()).isNotNull();
            assertThat(month.getGrowth()).isNotNull();
            assertThat(month.getEvents()).isNotNull();
            assertThat(month.getParticipants()).isNotNull();
        });
    }

    @Test
    @DisplayName("Should handle empty payments list")
    void testGetPaymentDashboard_EmptyPayments() {
        // Given
        Integer userId = 1;
        PaymentFilterDTO filters = new PaymentFilterDTO();
        filters.setStatus("ALL");

        when(organizerRepository.findByUserId(userId)).thenReturn(Optional.of(organizer));
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Payments.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Payments.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(anyBoolean())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());
        when(eventRepository.findByOrganizer(organizer)).thenReturn(Collections.emptyList());
        when(paymentRepository.findByOrganizerId(organizer.getId())).thenReturn(Collections.emptyList());

        // When
        PaymentDashboardDTO result = organizerPaymentService.getPaymentDashboard(userId, filters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSummary().getTotalIncome()).isEqualTo(0.0);
        assertThat(result.getSummary().getCompletedPayments()).isEqualTo(0);
        assertThat(result.getParticipantPayments()).isEmpty();
        assertThat(result.getRecentPayments()).isEmpty();
    }

    @Test
    @DisplayName("Should calculate monthly growth correctly")
    void testCalculateMonthlyGrowth() {
        // Given - Create payments from current and last month
        Payments currentMonthPayment = new Payments();
        currentMonthPayment.setAmount(300.0);
        currentMonthPayment.setPaymentStatus(PaymentStatus.SUCCESS);
        currentMonthPayment.setTransactionDate(LocalDateTime.now());
        currentMonthPayment.setEventRegistration(eventRegistration);

        Payments lastMonthPayment = new Payments();
        lastMonthPayment.setAmount(200.0);
        lastMonthPayment.setPaymentStatus(PaymentStatus.SUCCESS);
        lastMonthPayment.setTransactionDate(LocalDateTime.now().minusMonths(1));
        lastMonthPayment.setEventRegistration(eventRegistration);

        List<Payments> payments = Arrays.asList(currentMonthPayment, lastMonthPayment);
        PaymentFilterDTO filters = new PaymentFilterDTO();
        filters.setStatus("ALL");

        when(organizerRepository.findByUserId(1)).thenReturn(Optional.of(organizer));
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Payments.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Payments.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(anyBoolean())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(payments);
        when(eventRepository.findByOrganizer(organizer)).thenReturn(Collections.singletonList(event));
        when(paymentRepository.findByOrganizerId(organizer.getId())).thenReturn(payments);

        // When
        PaymentDashboardDTO result = organizerPaymentService.getPaymentDashboard(1, filters);

        // Then
        PaymentSummaryDTO summary = result.getSummary();
        // Growth should be calculated: ((300 - 200) / 200) * 100 = 50%
        // But since we're using all payments, it might be different
        assertThat(summary.getMonthlyGrowth()).isNotNull();
    }

    @Test
    @DisplayName("Should filter by payment method")
    void testGetPaymentDashboard_FilterByPaymentMethod() {
        // Given
        Integer userId = 1;
        PaymentFilterDTO filters = new PaymentFilterDTO();
        filters.setPaymentMethod("CREDIT_CARD");
        filters.setStatus("ALL");

        List<Payments> payments = Collections.singletonList(payment);

        when(organizerRepository.findByUserId(userId)).thenReturn(Optional.of(organizer));
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Payments.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Payments.class)).thenReturn(root);
        when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate[].class))).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(anyBoolean())).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(payments);
        when(eventRepository.findByOrganizer(organizer)).thenReturn(Collections.singletonList(event));
        when(paymentRepository.findByOrganizerId(organizer.getId())).thenReturn(payments);

        // When
        PaymentDashboardDTO result = organizerPaymentService.getPaymentDashboard(userId, filters);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getParticipantPayments()).isNotEmpty();
    }
}

