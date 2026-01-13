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
        // The incoming organizerId from the frontend is the userId; fetch the organizer and
        // use the organizer entity id for payment/event queries.
        Organizer organizer = organizerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Organizer not found with id: " + userId));
        Integer organizerEntityId = organizer.getId();

        log.info("Building dashboard for organizer: {}, filters: {}", userId, filters);

        // Get all payments for this organizer with filters
        List<Payments> allPayments = getFilteredPayments(organizerEntityId, filters);

        log.info("Found {} filtered payments", allPayments.size());

        // Build summary
        PaymentSummaryDTO summary = buildPaymentSummary(allPayments, filters);

        // Build event payments
        List<EventPaymentDTO> eventPayments = buildEventPayments(userId, filters);

        // Build participant payments
        List<ParticipantPaymentDTO> participantPayments = buildParticipantPayments(allPayments);

        // Get recent payments (last 10)
        List<ParticipantPaymentDTO> recentPayments = participantPayments.stream()
                .sorted((a, b) -> b.getPaymentDate().compareTo(a.getPaymentDate()))
                .limit(10)
                .collect(Collectors.toList());

        // Build revenue chart (last 6 months)
        List<MonthlyRevenueDTO> revenueChart = buildRevenueChart(organizerEntityId, filters);

        return PaymentDashboardDTO.builder()
                .summary(summary)
                .events(eventPayments)
                .participantPayments(participantPayments)
                .recentPayments(recentPayments)
                .revenueChart(revenueChart)
                .build();
    }

    private List<Payments> getFilteredPayments(Integer organizerId, PaymentFilterDTO filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Payments> cq = cb.createQuery(Payments.class);
        Root<Payments> payment = cq.from(Payments.class);

        // Important: fetch relations to avoid N+1
        Fetch<Payments, EventRegistration> regFetch = payment.fetch("eventRegistration", JoinType.INNER);
        regFetch.fetch("event", JoinType.INNER);
        regFetch.fetch("user", JoinType.LEFT);
        regFetch.fetch("eventParticipants", JoinType.LEFT);

        // For predicates, we need joins not fetches
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
                            cb.lessThanOrEqualTo(payment.get("transactionDate"), toDateTime)
                    );

                    Predicate eventDateInRange = cb.and(
                            cb.greaterThanOrEqualTo(event.get("date"), fromDate),
                            cb.lessThanOrEqualTo(event.get("date"), toDate)
                    );

                    predicates.add(cb.or(paymentDateInRange, eventDateInRange));
                } else if (filters.getDateRange().getFrom() != null) {
                    LocalDateTime fromDateTime = filters.getDateRange().getFrom().atStartOfDay();
                    LocalDate fromDate = filters.getDateRange().getFrom();

                    predicates.add(cb.or(
                            cb.greaterThanOrEqualTo(payment.get("transactionDate"), fromDateTime),
                            cb.greaterThanOrEqualTo(event.get("date"), fromDate)
                    ));
                } else if (filters.getDateRange().getTo() != null) {
                    LocalDateTime toDateTime = filters.getDateRange().getTo().atTime(23, 59, 59);
                    LocalDate toDate = filters.getDateRange().getTo();

                    predicates.add(cb.or(
                            cb.lessThanOrEqualTo(payment.get("transactionDate"), toDateTime),
                            cb.lessThanOrEqualTo(event.get("date"), toDate)
                    ));
                }
            }
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.distinct(true);

        List<Payments> result = entityManager.createQuery(cq).getResultList();
        log.info("getFilteredPayments returning {} payments", result.size());
        return result;
    }

    private PaymentSummaryDTO buildPaymentSummary(List<Payments> payments, PaymentFilterDTO filters) {
        double totalIncome = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Payments::getAmount)
                .sum();

        long completedPayments = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .count();

        long pendingPayments = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                .count();

        long refundedPayments = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.DECLINE ||
                        p.getPaymentStatus() == PaymentStatus.CANCEL)
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

        return PaymentSummaryDTO.builder()
                .totalIncome(totalIncome)
                .completedPayments((int) completedPayments)
                .pendingPayments((int) pendingPayments)
                .refundedPayments((int) refundedPayments)
                .monthlyGrowth(monthlyGrowth)
                .currency(CURRENCY)
                .totalParticipants(totalParticipants)
                .averagePayment(averagePayment)
                .platformFee((int) PLATFORM_FEE_PERCENTAGE)
                .build();
    }

    private double calculateMonthlyGrowth(List<Payments> payments) {
        LocalDate now = LocalDate.now();
        LocalDate lastMonth = now.minusMonths(1);

        double currentMonthRevenue = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
                .filter(p -> {
                    LocalDate paymentDate = p.getTransactionDate().toLocalDate();
                    return paymentDate.getMonth() == now.getMonth() &&
                            paymentDate.getYear() == now.getYear();
                })
                .mapToDouble(Payments::getAmount)
                .sum();

        double lastMonthRevenue = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
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
                                        if (from != null && regDate.isBefore(from)) return false;
                                        if (to != null && regDate.isAfter(to)) return false;
                                        return true;
                                    })
                                    .collect(Collectors.toList());
                        }
                    }

                    int totalParticipants = filteredRegistrations.stream()
                            .mapToInt(reg -> reg.getEventParticipants() != null ?
                                    reg.getEventParticipants().size() : 0)
                            .sum();

                    long paidParticipants = filteredRegistrations.stream()
                            .filter(reg -> reg.getPayments() != null &&
                                    reg.getPayments().getPaymentStatus() == PaymentStatus.SUCCESS)
                            .count();

                    double totalRevenue = filteredRegistrations.stream()
                            .filter(reg -> reg.getPayments() != null &&
                                    reg.getPayments().getPaymentStatus() == PaymentStatus.SUCCESS)
                            .mapToDouble(reg -> reg.getPayments().getAmount())
                            .sum();

                    double averagePaymentPerPerson = paidParticipants > 0 ?
                            totalRevenue / paidParticipants : 0.0;

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

                    int numberOfParticipants = reg.getEventParticipants() != null ?
                            reg.getEventParticipants().size() : 0;

                    String status = mapPaymentStatusToString(payment.getPaymentStatus());
                    String paymentMethod = mapPaymentMethodToString(payment.getMethod());

                    return ParticipantPaymentDTO.builder()
                            .id(payment.getId())
                            .participantName(reg.getContactName() != null ? reg.getContactName() :
                                    reg.getUser().getName())
                            .participantEmail(reg.getEmail() != null ? reg.getEmail() :
                                    reg.getUser().getEmail())
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
        allPayments = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS)
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

            double growth = previousRevenue > 0 ?
                    ((revenue - previousRevenue) / previousRevenue) * 100.0 :
                    (revenue > 0 ? 100.0 : 0.0);

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
                    .mapToInt(p -> p.getEventRegistration().getEventParticipants() != null ?
                            p.getEventRegistration().getEventParticipants().size() : 0)
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
        if (status == null) return null;
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
                return PaymentStatus.CANCEL;
            default:
                return null;
        }
    }

    private PaymentMethod mapPaymentMethodToEnum(String method) {
        if (method == null) return null;
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
        if (status == null) return "PENDING";
        switch (status) {
            case SUCCESS:
                return "COMPLETED";
            case PENDING:
                return "PENDING";
            case DECLINE:
                return "FAILED";
            case CANCEL:
                return "REFUNDED";
            default:
                return "PENDING";
        }
    }

    private String mapPaymentMethodToString(PaymentMethod method) {
        if (method == null) return "CREDIT_CARD";
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