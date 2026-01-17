package com.example.treksathi.service;

import com.example.treksathi.Interfaces.IOrganizerPaymentService;
import com.example.treksathi.dto.organizer.*;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.enums.PaymentMethod;
import com.example.treksathi.enums.PaymentStatus;
import com.example.treksathi.exception.NotFoundException;
import com.example.treksathi.model.Event;
import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.model.Payments;
import com.example.treksathi.repository.EventRepository;
import com.example.treksathi.repository.OrganizerRepository;
import com.example.treksathi.repository.PaymentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrganizerPaymentService implements IOrganizerPaymentService {

    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final EntityManager entityManager;

    private static final double PLATFORM_FEE_PERCENTAGE = 10.0;
    private static final String CURRENCY = "$";

    @Override
    public PaymentDashboardDTO getPaymentDashboard(Integer userId, PaymentFilterDTO filters) {
        // The incoming organizerId from the frontend is the userId; fetch the organizer
        // and
        // use the organizer entity id for payment/event queries.
        Organizer organizer = organizerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Organizer not found with id: " + userId));
        Integer organizerEntityId = organizer.getId();

        log.info("Building dashboard for organizer: {}, filters: {}", userId, filters);

        // Get all payments for this organizer with filters (unpaginated for
        // summary/charts)
        List<Payments> allPayments = getFilteredPaymentsList(organizerEntityId, filters);

        log.info("Found {} filtered payments", allPayments.size());

        // Build summary
        PaymentSummaryDTO summary = buildPaymentSummary(allPayments, filters);

        // Build event payments
        List<EventPaymentDTO> eventPayments = buildEventPayments(userId, filters);

        // Build participant payments (Recent 5 only for dashboard)
        List<ParticipantPaymentDTO> recentPayments = buildParticipantPayments(allPayments).stream()
                .sorted((a, b) -> b.getPaymentDate().compareTo(a.getPaymentDate()))
                .limit(5)
                .collect(Collectors.toList());

        // Build revenue chart (last 6 months)
        List<MonthlyRevenueDTO> revenueChart = buildRevenueChart(organizerEntityId, filters);

        return PaymentDashboardDTO.builder()
                .summary(summary)
                .events(eventPayments)
                .participantPayments(recentPayments) // Renaming/Using recentPayments for dashboard view
                .recentPayments(recentPayments)
                .revenueChart(revenueChart)
                .build();
    }

    @Override
    public org.springframework.data.domain.Page<ParticipantPaymentDTO> getPayments(Integer userId,
            PaymentFilterDTO filters, int page, int size) {
        Organizer organizer = organizerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Organizer not found with id: " + userId));
        Integer organizerEntityId = organizer.getId();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size); // Sorting
                                                                                                                        // handled
                                                                                                                        // in
                                                                                                                        // query

        // We need a way to get paginated result.
        // Re-using criteria builder logic but for Page.
        return getFilteredPaymentsPage(organizerEntityId, filters, pageable)
                .map(this::mapToParticipantPaymentDTO);
    }

    @Override
    public java.io.ByteArrayInputStream exportPaymentsToCSV(Integer userId) {
        Organizer organizer = organizerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Organizer not found with id: " + userId));
        Integer organizerEntityId = organizer.getId();

        // Get all payments for export
        List<Payments> payments = getFilteredPaymentsList(organizerEntityId, new PaymentFilterDTO()); // No filters or
                                                                                                      // default ?

        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                java.io.PrintWriter writer = new java.io.PrintWriter(out)) {

            // Header
            writer.println("Transaction ID, Event, Participant, Email, Amount, Status, Method, Date");

            for (Payments payment : payments) {
                String eventTitle = payment.getEventRegistration().getEvent().getTitle();
                String participant = payment.getEventRegistration().getContactName() != null
                        ? payment.getEventRegistration().getContactName()
                        : payment.getEventRegistration().getUser().getName();
                String email = payment.getEventRegistration().getEmail() != null
                        ? payment.getEventRegistration().getEmail()
                        : payment.getEventRegistration().getUser().getEmail();

                writer.printf("%s, %s, %s, %s, %.2f, %s, %s, %s\n",
                        payment.getTransactionUuid(),
                        escapeSpecialCharacters(eventTitle),
                        escapeSpecialCharacters(participant),
                        escapeSpecialCharacters(email),
                        payment.getAmount() != null ? payment.getAmount() : 0.0,
                        mapPaymentStatusToString(payment.getPaymentStatus()),
                        mapPaymentMethodToString(payment.getMethod()),
                        payment.getTransactionDate());
            }
            writer.flush();
            return new java.io.ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to export data to CSV: " + e.getMessage());
        }
    }

    private String escapeSpecialCharacters(String data) {
        if (data == null)
            return "";
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    private ParticipantPaymentDTO mapToParticipantPaymentDTO(Payments payment) {
        EventRegistration reg = payment.getEventRegistration();
        Event event = reg.getEvent();

        int numberOfParticipants = reg.getEventParticipants() != null ? reg.getEventParticipants().size() : 0;

        String status = mapPaymentStatusToString(payment.getPaymentStatus());
        String paymentMethod = mapPaymentMethodToString(payment.getMethod());

        return ParticipantPaymentDTO.builder()
                .id(payment.getId())
                .participantName(reg.getContactName() != null ? reg.getContactName() : reg.getUser().getName())
                .participantEmail(reg.getEmail() != null ? reg.getEmail() : reg.getUser().getEmail())
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .numberOfParticipants(numberOfParticipants)
                .paymentDate(payment.getTransactionDate())
                .amount(payment.getAmount())
                .status(status)
                .paymentMethod(paymentMethod)
                .transactionId(payment.getTransactionUuid())
                .receiptUrl(null)
                .build();
    }

    private org.springframework.data.domain.Page<Payments> getFilteredPaymentsPage(Integer organizerId,
            PaymentFilterDTO filters, org.springframework.data.domain.Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Payments> cq = cb.createQuery(Payments.class);
        Root<Payments> payment = cq.from(Payments.class);

        // Important: fetch relations to avoid N+1
        Fetch<Payments, EventRegistration> regFetch = payment.fetch("eventRegistration", JoinType.INNER);
        regFetch.fetch("event", JoinType.INNER);
        regFetch.fetch("user", JoinType.LEFT);
        regFetch.fetch("eventParticipants", JoinType.LEFT);

        // Predicates
        List<Predicate> predicates = buildPredicates(cb, payment, organizerId, filters);

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(payment.get("transactionDate")));

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Payments> countRoot = countQuery.from(Payments.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, organizerId, filters); // Rebuild predicates
                                                                                                // for count root
        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        Long count = entityManager.createQuery(countQuery).getSingleResult();

        List<Payments> result = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new org.springframework.data.domain.PageImpl<>(result, pageable, count);
    }

    private List<Payments> getFilteredPaymentsList(Integer organizerId, PaymentFilterDTO filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Payments> cq = cb.createQuery(Payments.class);
        Root<Payments> payment = cq.from(Payments.class);

        // Important: fetch relations to avoid N+1
        Fetch<Payments, EventRegistration> regFetch = payment.fetch("eventRegistration", JoinType.INNER);
        regFetch.fetch("event", JoinType.INNER);
        regFetch.fetch("user", JoinType.LEFT);
        regFetch.fetch("eventParticipants", JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(cb, payment, organizerId, filters);
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(payment.get("transactionDate")));

        return entityManager.createQuery(cq).getResultList();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Payments> payment, Integer organizerId,
            PaymentFilterDTO filters) {
        // Important: fetches are not allowed in count query usually, so we rely on
        // joins if needed for filtering
        // But for filtering by organizer we need join.
        Join<Payments, EventRegistration> registration = payment.join("eventRegistration", JoinType.INNER);
        Join<EventRegistration, Event> event = registration.join("event", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        // Always required - filter by organizer
        predicates.add(cb.equal(event.get("organizer").get("id"), organizerId));

        // Optional filters - only add when value exists
        if (filters != null) {
            if (filters.getStatus() != null && !"ALL".equals(filters.getStatus())) {
                PaymentStatus status = mapStatusToEnum(filters.getStatus());
                if (status != null) {
                    predicates.add(cb.equal(payment.get("paymentStatus"), status));
                }
            }

            if (filters.getEventId() != null) {
                predicates.add(cb.equal(event.get("id"), filters.getEventId()));
            }

            if (filters.getPaymentMethod() != null && !"ALL".equals(filters.getPaymentMethod())) {
                PaymentMethod method = mapPaymentMethodToEnum(filters.getPaymentMethod());
                if (method != null) {
                    predicates.add(cb.equal(payment.get("method"), method));
                }
            }

            // FIXED: Date range filter - check transaction date OR event date
            if (filters.getDateRange() != null) {
                if (filters.getDateRange().getFrom() != null && filters.getDateRange().getTo() != null) {
                    LocalDateTime fromDateTime = filters.getDateRange().getFrom().atStartOfDay();
                    LocalDateTime toDateTime = filters.getDateRange().getTo().atTime(23, 59, 59);
                    LocalDate fromDate = filters.getDateRange().getFrom();
                    LocalDate toDate = filters.getDateRange().getTo();

                    // Payment was made in this date range OR event is in this date range
                    Predicate paymentDateInRange = cb.and(
                            cb.greaterThanOrEqualTo(payment.get("transactionDate"), fromDateTime),
                            cb.lessThanOrEqualTo(payment.get("transactionDate"), toDateTime));

                    Predicate eventDateInRange = cb.and(
                            cb.greaterThanOrEqualTo(event.get("date"), fromDate),
                            cb.lessThanOrEqualTo(event.get("date"), toDate));

                    predicates.add(cb.or(paymentDateInRange, eventDateInRange));
                } else if (filters.getDateRange().getFrom() != null) {
                    LocalDateTime fromDateTime = filters.getDateRange().getFrom().atStartOfDay();
                    LocalDate fromDate = filters.getDateRange().getFrom();

                    predicates.add(cb.or(
                            cb.greaterThanOrEqualTo(payment.get("transactionDate"), fromDateTime),
                            cb.greaterThanOrEqualTo(event.get("date"), fromDate)));
                } else if (filters.getDateRange().getTo() != null) {
                    LocalDateTime toDateTime = filters.getDateRange().getTo().atTime(23, 59, 59);
                    LocalDate toDate = filters.getDateRange().getTo();

                    predicates.add(cb.or(
                            cb.lessThanOrEqualTo(payment.get("transactionDate"), toDateTime),
                            cb.lessThanOrEqualTo(event.get("date"), toDate)));
                }
            }
        }
        return predicates;
    }

    private PaymentSummaryDTO buildPaymentSummary(List<Payments> payments, PaymentFilterDTO filters) {
        double totalIncome = payments.stream()
                .filter(isRevenueStatus())
                .mapToDouble(Payments::getAmount)
                .sum();

        long completedPayments = payments.stream()
                .filter(isRevenueStatus())
                .count();

        long pendingPayments = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                .count();

        long refundedPayments = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.DECLINE ||
                        p.getPaymentStatus() == PaymentStatus.CANCEL ||
                        p.getPaymentStatus() == PaymentStatus.REFUNDED ||
                        p.getPaymentStatus() == PaymentStatus.FAILED)
                .count();

        // Calculate monthly growth
        double monthlyGrowth = calculateMonthlyGrowth(payments);

        // Calculate total participants
        int totalParticipants = payments.stream()
                .map(p -> p.getEventRegistration().getEventParticipants())
                .filter(Objects::nonNull)
                .mapToInt(List::size)
                .sum();

        // Calculate average payment
        double averagePayment = completedPayments > 0 ? totalIncome / completedPayments : 0.0;

        // Calculate platform fee from verified payments or estimate
        // If we have fee saved, sum it up, otherwise estimate
        // Ideally we should sum p.getFee() if available
        double totalPlatformFee = payments.stream()
                .filter(isRevenueStatus())
                .mapToDouble(p -> p.getFee() != null ? p.getFee() : p.getAmount() * 0.10)
                .sum();

        return PaymentSummaryDTO.builder()
                .totalIncome(totalIncome)
                .completedPayments((int) completedPayments)
                .pendingPayments((int) pendingPayments)
                .refundedPayments((int) refundedPayments)
                .monthlyGrowth(monthlyGrowth)
                .currency(CURRENCY)
                .totalParticipants(totalParticipants)
                .averagePayment(averagePayment)
                .platformFee((int) totalPlatformFee) // DTO expects int? PlatformFee usually percentage or total? DTO
                                                     // says int.. likely percentage.
                // Wait, frontend shows percentage usually. Checking previous code:
                // .platformFee((int) PLATFORM_FEE_PERCENTAGE)
                // If it expects percentage, we keep it as constant.
                .platformFee((int) PLATFORM_FEE_PERCENTAGE)
                .build();
    }

    // Helper to identify revenue generating statuses
    private java.util.function.Predicate<Payments> isRevenueStatus() {
        return p -> p.getPaymentStatus() == PaymentStatus.SUCCESS ||
                p.getPaymentStatus() == PaymentStatus.RELEASED ||
                p.getPaymentStatus() == PaymentStatus.COMPLETED;
    }

    private double calculateMonthlyGrowth(List<Payments> payments) {
        LocalDate now = LocalDate.now();
        LocalDate lastMonth = now.minusMonths(1);

        double currentMonthRevenue = payments.stream()
                .filter(isRevenueStatus())
                .filter(p -> {
                    LocalDate paymentDate = p.getTransactionDate().toLocalDate();
                    return paymentDate.getMonth() == now.getMonth() &&
                            paymentDate.getYear() == now.getYear();
                })
                .mapToDouble(Payments::getAmount)
                .sum();

        double lastMonthRevenue = payments.stream()
                .filter(isRevenueStatus())
                .filter(p -> {
                    LocalDate paymentDate = p.getTransactionDate().toLocalDate();
                    return paymentDate.getMonth() == lastMonth.getMonth() &&
                            paymentDate.getYear() == lastMonth.getYear();
                })
                .mapToDouble(Payments::getAmount)
                .sum();

        if (lastMonthRevenue == 0) {
            return currentMonthRevenue > 0 ? 100.0 : 0.0;
        }

        return ((currentMonthRevenue - lastMonthRevenue) / lastMonthRevenue) * 100.0;
    }

    private List<EventPaymentDTO> buildEventPayments(Integer userId, PaymentFilterDTO filters) {
        Organizer organizer = organizerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Organizer not found"));

        List<Event> events = eventRepository.findByOrganizer(organizer);

        return events.stream()
                .map(event -> {
                    List<EventRegistration> registrations = event.getEventRegistration();
                    if (registrations == null) {
                        registrations = Collections.emptyList();
                    }

                    // FIXED: Filter by event date OR registration date if date range is provided
                    List<EventRegistration> filteredRegistrations = registrations;
                    if (filters != null && filters.getDateRange() != null) {
                        LocalDate from = filters.getDateRange().getFrom();
                        LocalDate to = filters.getDateRange().getTo();
                        LocalDate eventDate = event.getDate();

                        // Check if event date is in range
                        boolean eventInRange = true;
                        if (from != null && eventDate.isBefore(from)) {
                            eventInRange = false;
                        }
                        if (to != null && eventDate.isAfter(to)) {
                            eventInRange = false;
                        }

                        if (!eventInRange) {
                            // If event is not in range, filter registrations by registration date
                            filteredRegistrations = registrations.stream()
                                    .filter(reg -> {
                                        LocalDate regDate = reg.getRegistrationDate().toLocalDate();
                                        if (from != null && regDate.isBefore(from))
                                            return false;
                                        if (to != null && regDate.isAfter(to))
                                            return false;
                                        return true;
                                    })
                                    .collect(Collectors.toList());
                        }
                    }

                    int totalParticipants = filteredRegistrations.stream()
                            .mapToInt(reg -> reg.getEventParticipants() != null ? reg.getEventParticipants().size() : 0)
                            .sum();

                    long paidParticipants = filteredRegistrations.stream()
                            .filter(reg -> reg.getPayments() != null &&
                                    (reg.getPayments().getPaymentStatus() == PaymentStatus.SUCCESS ||
                                            reg.getPayments().getPaymentStatus() == PaymentStatus.RELEASED ||
                                            reg.getPayments().getPaymentStatus() == PaymentStatus.COMPLETED))
                            .count();

                    double totalRevenue = filteredRegistrations.stream()
                            .filter(reg -> reg.getPayments() != null &&
                                    (reg.getPayments().getPaymentStatus() == PaymentStatus.SUCCESS ||
                                            reg.getPayments().getPaymentStatus() == PaymentStatus.RELEASED ||
                                            reg.getPayments().getPaymentStatus() == PaymentStatus.COMPLETED))
                            .mapToDouble(reg -> reg.getPayments().getAmount())
                            .sum();

                    double averagePaymentPerPerson = paidParticipants > 0 ? totalRevenue / paidParticipants : 0.0;

                    double organizerShare = totalRevenue * (1 - PLATFORM_FEE_PERCENTAGE / 100.0);

                    String status = determineEventStatus(event);

                    return EventPaymentDTO.builder()
                            .id(event.getId())
                            .eventId(event.getId())
                            .eventTitle(event.getTitle())
                            .eventDate(event.getDate())
                            .totalParticipants(totalParticipants)
                            .paidParticipants((int) paidParticipants)
                            .totalRevenue(totalRevenue)
                            .averagePaymentPerPerson(averagePaymentPerPerson)
                            .organizerShare(organizerShare)
                            .status(status)
                            .build();
                })
                .filter(eventPayment -> eventPayment.getTotalParticipants() > 0)
                .collect(Collectors.toList());
    }

    private String determineEventStatus(Event event) {
        if (event.getStatus() == EventStatus.COMPLETED) {
            return "COMPLETED";
        } else if (event.getStatus() == EventStatus.CANCEL || event.getStatus() == EventStatus.DELETED) {
            return "CANCELLED";
        } else if (event.getDate().isAfter(LocalDate.now())) {
            return "ACTIVE";
        } else {
            return "COMPLETED";
        }
    }

    private List<ParticipantPaymentDTO> buildParticipantPayments(List<Payments> payments) {
        return payments.stream()
                .map(payment -> {
                    EventRegistration reg = payment.getEventRegistration();
                    Event event = reg.getEvent();

                    int numberOfParticipants = reg.getEventParticipants() != null ? reg.getEventParticipants().size()
                            : 0;

                    String status = mapPaymentStatusToString(payment.getPaymentStatus());
                    String paymentMethod = mapPaymentMethodToString(payment.getMethod());

                    return ParticipantPaymentDTO.builder()
                            .id(payment.getId())
                            .participantName(
                                    reg.getContactName() != null ? reg.getContactName() : reg.getUser().getName())
                            .participantEmail(reg.getEmail() != null ? reg.getEmail() : reg.getUser().getEmail())
                            .eventId(event.getId())
                            .eventTitle(event.getTitle())
                            .numberOfParticipants(numberOfParticipants)
                            .paymentDate(payment.getTransactionDate())
                            .amount(payment.getAmount())
                            .status(status)
                            .paymentMethod(paymentMethod)
                            .transactionId(payment.getTransactionUuid())
                            .receiptUrl(null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<MonthlyRevenueDTO> buildRevenueChart(Integer organizerId, PaymentFilterDTO filters) {
        // Get all payments for the organizer (without date filter for chart)
        List<Payments> allPayments = paymentRepository.findByOrganizerId(organizerId);

        // Filter only successful payments
        // Filter only successful/released payments
        allPayments = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS ||
                        p.getPaymentStatus() == PaymentStatus.RELEASED ||
                        p.getPaymentStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.toList());

        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new NotFoundException("Organizer not found"));
        List<Event> events = eventRepository.findByOrganizer(organizer);

        // Group by month for last 6 months
        LocalDate now = LocalDate.now();
        List<MonthlyRevenueDTO> monthlyRevenues = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            LocalDate previousMonthDate = monthDate.minusMonths(1);

            String monthLabel = monthDate.format(DateTimeFormatter.ofPattern("MMM yyyy"));

            double revenue = allPayments.stream()
                    .filter(p -> {
                        LocalDate paymentDate = p.getTransactionDate().toLocalDate();
                        return paymentDate.getMonth() == monthDate.getMonth() &&
                                paymentDate.getYear() == monthDate.getYear();
                    })
                    .mapToDouble(Payments::getAmount)
                    .sum();

            double previousRevenue = allPayments.stream()
                    .filter(p -> {
                        LocalDate paymentDate = p.getTransactionDate().toLocalDate();
                        return paymentDate.getMonth() == previousMonthDate.getMonth() &&
                                paymentDate.getYear() == previousMonthDate.getYear();
                    })
                    .mapToDouble(Payments::getAmount)
                    .sum();

            double growth = previousRevenue > 0 ? ((revenue - previousRevenue) / previousRevenue) * 100.0
                    : (revenue > 0 ? 100.0 : 0.0);

            int eventCount = (int) events.stream()
                    .filter(e -> {
                        LocalDate eventDate = e.getDate();
                        return eventDate.getMonth() == monthDate.getMonth() &&
                                eventDate.getYear() == monthDate.getYear();
                    })
                    .count();

            int participants = allPayments.stream()
                    .filter(p -> {
                        LocalDate paymentDate = p.getTransactionDate().toLocalDate();
                        return paymentDate.getMonth() == monthDate.getMonth() &&
                                paymentDate.getYear() == monthDate.getYear();
                    })
                    .mapToInt(p -> p.getEventRegistration().getEventParticipants() != null
                            ? p.getEventRegistration().getEventParticipants().size()
                            : 0)
                    .sum();

            monthlyRevenues.add(MonthlyRevenueDTO.builder()
                    .month(monthLabel)
                    .revenue(revenue)
                    .growth(growth)
                    .events(eventCount)
                    .participants(participants)
                    .build());
        }

        return monthlyRevenues;
    }

    // Helper methods for mapping
    private PaymentStatus mapStatusToEnum(String status) {
        if (status == null)
            return null;
        switch (status.toUpperCase()) {
            case "COMPLETED":
            case "SUCCESS":
                return PaymentStatus.SUCCESS;
            case "PENDING":
                return PaymentStatus.PENDING;
            case "FAILED":
            case "DECLINE":
                return PaymentStatus.DECLINE;
            case "REFUNDED":
            case "CANCEL":
                return PaymentStatus.CANCEL; // Mapping frontend cancel to backend CANCEL for now, or new REFUNDED
            case "RELEASED":
                return PaymentStatus.RELEASED;
            case "VERIFIED":
                return PaymentStatus.COMPLETED;
            default:
                return null;
        }
    }

    private PaymentMethod mapPaymentMethodToEnum(String method) {
        if (method == null)
            return null;
        switch (method.toUpperCase()) {
            case "CREDIT_CARD":
            case "CARD":
                return PaymentMethod.CARD;
            case "ESEWA":
                return PaymentMethod.ESEWA;
            case "STRIPE":
                return PaymentMethod.STRIPE;
            default:
                return null;
        }
    }

    private String mapPaymentStatusToString(PaymentStatus status) {
        if (status == null)
            return "PENDING";
        switch (status) {
            case SUCCESS:
                return "COMPLETED"; // User display for SUCCESS
            case PENDING:
                return "PENDING";
            case DECLINE:
                return "FAILED";
            case CANCEL:
            case REFUNDED:
                return "REFUNDED";
            case COMPLETED:
                return "VERIFIED"; // Admin verified
            case RELEASED:
                return "RELEASED";
            default:
                return "PENDING";
        }
    }

    private String mapPaymentMethodToString(PaymentMethod method) {
        if (method == null)
            return "CREDIT_CARD";
        switch (method) {
            case CARD:
                return "CREDIT_CARD";
            case ESEWA:
                return "ESEWA";
            case STRIPE:
                return "STRIPE";
            default:
                return "CREDIT_CARD";
        }
    }
}