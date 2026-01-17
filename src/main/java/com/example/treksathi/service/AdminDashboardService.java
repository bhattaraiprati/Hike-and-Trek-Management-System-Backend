package com.example.treksathi.service;

import com.example.treksathi.dto.admin.dashboard.*;
import com.example.treksathi.enums.AccountStatus;
import com.example.treksathi.enums.Approval_status;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.enums.PaymentStatus;
import com.example.treksathi.enums.Role;
import com.example.treksathi.model.*;
import com.example.treksathi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;
    private final EventRepository eventRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    public AdminDashboardDTO getDashboardData() {
        log.info("Generating admin dashboard data");

        AdminStatsDTO stats = calculateStats();
        List<RevenueAnalyticsDTO> revenueChart = buildRevenueChart();
        List<UserGrowthDTO> userGrowth = buildUserGrowthChart();
        List<AdminRecentActivityDTO> recentActivities = buildRecentActivities();

        return AdminDashboardDTO.builder()
                .stats(stats)
                .revenueChart(revenueChart)
                .userGrowth(userGrowth)
                .recentActivities(recentActivities)
                .build();
    }

    private AdminStatsDTO calculateStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(AccountStatus.ACTIVE);
        long totalOrganizers = organizerRepository.count();
        long pendingOrganizers = organizerRepository.countByApprovalStatus(Approval_status.PENDING);
        long totalEvents = eventRepository.count();
        long activeEvents = eventRepository.countByStatus(EventStatus.ACTIVE);

        // Revenue calculations
        List<Payments> allSuccessfulPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS ||
                        p.getPaymentStatus() == PaymentStatus.RELEASED ||
                        p.getPaymentStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.toList());

        double totalRevenue = allSuccessfulPayments.stream()
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                .sum();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        double monthlyRevenue = allSuccessfulPayments.stream()
                .filter(p -> p.getTransactionDate() != null && p.getTransactionDate().isAfter(firstDayOfMonth))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                .sum();

        // Growth percentages (simplified comparison with previous 30 days vs current 30
        // days or similar)
        double revenueGrowth = calculateRevenueGrowth(allSuccessfulPayments);
        double userGrowth = calculateUserGrowth();
        double eventGrowth = calculateEventGrowth();

        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalOrganizers(totalOrganizers)
                .pendingOrganizers(pendingOrganizers)
                .totalEvents(totalEvents)
                .activeEvents(activeEvents)
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .revenueGrowth(revenueGrowth)
                .userGrowth(userGrowth)
                .eventGrowth(eventGrowth)
                .build();
    }

    private List<RevenueAnalyticsDTO> buildRevenueChart() {
        List<RevenueAnalyticsDTO> chart = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthLabel = monthDate.format(DateTimeFormatter.ofPattern("MMM"));

            LocalDateTime start = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = monthDate.withDayOfMonth(monthDate.lengthOfMonth()).atTime(23, 59, 59);

            List<Payments> monthlyPayments = paymentRepository.findAllByTransactionDateBetween(start, end).stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS ||
                            p.getPaymentStatus() == PaymentStatus.RELEASED ||
                            p.getPaymentStatus() == PaymentStatus.COMPLETED)
                    .collect(Collectors.toList());

            double revenue = monthlyPayments.stream()
                    .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();
            double fees = monthlyPayments.stream()
                    .mapToDouble(p -> p.getFee() != null ? p.getFee() : p.getAmount() * 0.10).sum();
            double earnings = revenue - fees;

            chart.add(new RevenueAnalyticsDTO(monthLabel, revenue, earnings, fees));
        }

        return chart;
    }

    private List<UserGrowthDTO> buildUserGrowthChart() {
        List<UserGrowthDTO> chart = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthLabel = monthDate.format(DateTimeFormatter.ofPattern("MMM"));

            LocalDateTime start = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = monthDate.withDayOfMonth(monthDate.lengthOfMonth()).atTime(23, 59, 59);

            long hikers = userRepository.countByRoleAndCreatedAtBetween(Role.HIKER, start, end);
            long organizers = userRepository.countByRoleAndCreatedAtBetween(Role.ORGANIZER, start, end);

            chart.add(new UserGrowthDTO(monthLabel, hikers, organizers));
        }

        return chart;
    }

    private List<AdminRecentActivityDTO> buildRecentActivities() {
        List<AdminRecentActivityDTO> activities = new ArrayList<>();

        // Recent User Registrations
        userRepository.findTop5ByOrderByCreatedAtDesc().forEach(u -> {
            activities.add(AdminRecentActivityDTO.builder()
                    .id(u.getId())
                    .type("USER_REGISTER")
                    .title("New User Joined")
                    .description(u.getName() + " registered as " + u.getRole())
                    .timestamp(u.getCreatedAt())
                    .status(u.getStatus().toString())
                    .relatedId(u.getId())
                    .build());
        });

        // Recent Payments
        paymentRepository.findTop5ByOrderByTransactionDateDesc().forEach(p -> {
            activities.add(AdminRecentActivityDTO.builder()
                    .id(p.getId())
                    .type("PAYMENT")
                    .title("Payment Received")
                    .description("Amount: $" + p.getAmount() + " via " + p.getMethod())
                    .timestamp(p.getTransactionDate())
                    .status(p.getPaymentStatus().toString())
                    .relatedId(p.getId())
                    .build());
        });

        // Recent Events
        eventRepository.findTop5ByOrderByCreatedAtDesc().forEach(e -> {
            activities.add(AdminRecentActivityDTO.builder()
                    .id(e.getId())
                    .type("EVENT_CREATE")
                    .title("New Event Created")
                    .description(e.getTitle() + " by " + e.getOrganizer().getOrganization_name())
                    .timestamp(e.getCreatedAt())
                    .status(e.getStatus().toString())
                    .relatedId(e.getId())
                    .build());
        });

        // Recent Reviews
        reviewRepository.findTop5ByOrderByCreatedAtDesc().forEach(r -> {
            activities.add(AdminRecentActivityDTO.builder()
                    .id(r.getId())
                    .type("REVIEW")
                    .title("New Review")
                    .description(r.getComment())
                    .timestamp(r.getCreatedAt())
                    .status("COMPLETED")
                    .relatedId(r.getEvents().getId())
                    .build());
        });

        return activities.stream()
                .filter(a -> a.getTimestamp() != null) // Filter out activities with null timestamps
                .sorted(Comparator.comparing(AdminRecentActivityDTO::getTimestamp, Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private double calculateRevenueGrowth(List<Payments> allPayments) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime prevMonthStart = monthStart.minusMonths(1);

        double curMonthRev = allPayments.stream()
                .filter(p -> p.getTransactionDate() != null && p.getTransactionDate().isAfter(monthStart))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();

        double prevMonthRev = allPayments.stream()
                .filter(p -> p.getTransactionDate() != null && p.getTransactionDate().isAfter(prevMonthStart)
                        && p.getTransactionDate().isBefore(monthStart))
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();

        if (prevMonthRev == 0)
            return curMonthRev > 0 ? 100 : 0;
        return ((curMonthRev - prevMonthRev) / prevMonthRev) * 100;
    }

    private double calculateUserGrowth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime prevMonthStart = monthStart.minusMonths(1);

        long curMonthUsers = userRepository.countByCreatedAtAfter(monthStart);
        long prevMonthUsers = userRepository.countByCreatedAtBetween(prevMonthStart, monthStart);

        if (prevMonthUsers == 0)
            return curMonthUsers > 0 ? 100 : 0;
        return ((double) (curMonthUsers - prevMonthUsers) / prevMonthUsers) * 100;
    }

    private double calculateEventGrowth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime prevMonthStart = monthStart.minusMonths(1);

        long curMonthEvents = eventRepository.countByCreatedAtAfter(monthStart);
        long prevMonthEvents = eventRepository.countByCreatedAtBetween(prevMonthStart, monthStart);

        if (prevMonthEvents == 0)
            return curMonthEvents > 0 ? 100 : 0;
        return ((double) (curMonthEvents - prevMonthEvents) / prevMonthEvents) * 100;
    }
}
