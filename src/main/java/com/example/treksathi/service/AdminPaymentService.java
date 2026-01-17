package com.example.treksathi.service;

import com.example.treksathi.dto.payment.AdminPaymentDTO;
import com.example.treksathi.dto.payment.OrganizerBalanceDTO;
import com.example.treksathi.dto.payment.PaymentStatsDTO;
import com.example.treksathi.enums.PaymentStatus;
import com.example.treksathi.model.Payments;
import com.example.treksathi.model.User;
import com.example.treksathi.repository.PaymentRepository;
import com.example.treksathi.repository.UserRepository;
import com.example.treksathi.specification.PaymentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPaymentService {

        private final PaymentRepository paymentRepository;
        private final UserRepository userRepository;

        public Page<AdminPaymentDTO> getAllPayments(int page, int size, String status, String method, String search,
                        Integer organizerId,
                        LocalDate startDate, LocalDate endDate) {
                Pageable pageable = PageRequest.of(page, size);
                LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
                LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

                Specification<Payments> spec = PaymentSpecification.getPayments(status, method, search, organizerId,
                                startDateTime,
                                endDateTime);
                Page<Payments> paymentPage = paymentRepository.findAll(spec, pageable);

                return paymentPage.map(this::mapToAdminPaymentDTO);
        }

        public PaymentStatsDTO getPaymentStats() {
                List<Payments> allPayments = paymentRepository.findAll();

                double totalRevenue = allPayments.stream().mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                                .sum();
                long pendingPayments = allPayments.stream().filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                                .count();
                long completedPayments = allPayments.stream().filter(
                                p -> p.getPaymentStatus() == PaymentStatus.COMPLETED
                                                || p.getPaymentStatus() == PaymentStatus.SUCCESS)
                                .count();
                long releasedPayments = allPayments.stream().filter(p -> p.getPaymentStatus() == PaymentStatus.RELEASED)
                                .count();

                double totalFee = allPayments.stream().mapToDouble(p -> p.getFee() != null ? p.getFee() : 0.0).sum();
                double netRevenue = allPayments.stream()
                                .mapToDouble(p -> p.getNetAmount() != null ? p.getNetAmount() : 0.0)
                                .sum();

                // Calculate today revenue
                LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
                double todayRevenue = allPayments.stream()
                                .filter(p -> p.getTransactionDate() != null
                                                && p.getTransactionDate().isAfter(startOfDay))
                                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                                .sum();

                double averagePayment = allPayments.isEmpty() ? 0 : totalRevenue / allPayments.size();

                return PaymentStatsDTO.builder()
                                .totalRevenue(totalRevenue)
                                .pendingPayments(pendingPayments)
                                .completedPayments(completedPayments)
                                .releasedPayments(releasedPayments)
                                .totalFee(totalFee)
                                .netRevenue(netRevenue)
                                .todayRevenue(todayRevenue)
                                .averagePayment(averagePayment)
                                .build();
        }

        public List<OrganizerBalanceDTO> getOrganizerBalances() {
                List<Payments> allPayments = paymentRepository.findAll();

                // Group by Organizer ID
                Map<Integer, List<Payments>> paymentsByOrganizer = allPayments.stream()
                                .filter(p -> p.getEventRegistration() != null &&
                                                p.getEventRegistration().getEvent() != null &&
                                                p.getEventRegistration().getEvent().getOrganizer() != null)
                                .collect(Collectors.groupingBy(
                                                p -> p.getEventRegistration().getEvent().getOrganizer().getId()));

                return paymentsByOrganizer.entrySet().stream()
                                .map(entry -> {
                                        int organizerId = entry.getKey();
                                        List<Payments> payments = entry.getValue();
                                        var organizer = payments.get(0).getEventRegistration().getEvent()
                                                        .getOrganizer();

                                        double pendingAmount = payments.stream()
                                                        .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED
                                                                        || p.getPaymentStatus() == PaymentStatus.SUCCESS)
                                                        .mapToDouble(p -> p.getNetAmount() != null ? p.getNetAmount()
                                                                        : 0.0)
                                                        .sum();

                                        double releasedAmount = payments.stream()
                                                        .filter(p -> p.getPaymentStatus() == PaymentStatus.RELEASED)
                                                        .mapToDouble(p -> p.getNetAmount() != null ? p.getNetAmount()
                                                                        : 0.0)
                                                        .sum();

                                        long pendingPaymentsCount = payments.stream()
                                                        .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED
                                                                        || p.getPaymentStatus() == PaymentStatus.SUCCESS)
                                                        .count();

                                        return OrganizerBalanceDTO.builder()
                                                        .organizerId(organizerId)
                                                        .organizerName(organizer.getUser().getName()) // assuming
                                                                                                      // contact person
                                                                                                      // name/user name
                                                        .organization(organizer.getOrganization_name())
                                                        .pendingAmount(pendingAmount)
                                                        .releasedAmount(releasedAmount)
                                                        .totalBalance(pendingAmount + releasedAmount) // Total earnings
                                                                                                      // so far
                                                        .pendingPayments(pendingPaymentsCount)
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        @Transactional
        public AdminPaymentDTO verifyPayment(int paymentId, int adminId) {
                Payments payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new RuntimeException("Payment not found"));

                User admin = userRepository.findById(adminId)
                                .orElseThrow(() -> new RuntimeException("Admin user not found"));

                if (payment.getPaymentStatus() == PaymentStatus.PENDING) {
                        payment.setPaymentStatus(PaymentStatus.COMPLETED);
                        payment.setVerifiedBy(admin);
                        payment.setVerifiedAt(LocalDateTime.now());

                        // Calculate fees if not already set (assuming 5% fee logic as seen in frontend
                        // mock)
                        if (payment.getFee() == null || payment.getFee() == 0) {
                                double amount = payment.getAmount() != null ? payment.getAmount() : 0.0;
                                double fee = amount * 0.05;
                                payment.setFee(fee);
                                payment.setNetAmount(amount - fee);
                        }

                        Payments saved = paymentRepository.save(payment);
                        return mapToAdminPaymentDTO(saved);
                } else {
                        throw new RuntimeException("Payment is not in PENDING status");
                }
        }

        @Transactional
        public AdminPaymentDTO releasePayment(int paymentId, int adminId, String notes) {
                Payments payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new RuntimeException("Payment not found"));

                User admin = userRepository.findById(adminId)
                                .orElseThrow(() -> new RuntimeException("Admin user not found"));

                // Allow release if it's COMPLETED or SUCCESS
                if (payment.getPaymentStatus() == PaymentStatus.COMPLETED
                                || payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                        payment.setPaymentStatus(PaymentStatus.RELEASED);
                        payment.setReleasedBy(admin);
                        payment.setReleasedAt(LocalDateTime.now());
                        payment.setReleaseNotes(notes);

                        Payments saved = paymentRepository.save(payment);
                        return mapToAdminPaymentDTO(saved);
                } else {
                        throw new RuntimeException("Payment must be verified (COMPLETED) before release");
                }
        }

        @Transactional
        public void bulkRelease(List<Integer> paymentIds, int adminId) {
                for (Integer id : paymentIds) {
                        try {
                                releasePayment(id, adminId, "Bulk Release");
                        } catch (Exception e) {
                                log.error("Failed to release payment {}", id, e);
                        }
                }
        }

        @Transactional
        public AdminPaymentDTO refundPayment(int paymentId, int adminId) {
                Payments payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new RuntimeException("Payment not found"));

                // Ensure only refundable statuses can be refunded (e.g., PENDING or COMPLETED)
                if (payment.getPaymentStatus() == PaymentStatus.PENDING
                                || payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                        payment.setPaymentStatus(PaymentStatus.REFUNDED);
                        // Logic to process actual refund via gateway would go here
                        // For now, just updating DB status
                        return mapToAdminPaymentDTO(paymentRepository.save(payment));
                } else {
                        throw new RuntimeException("Cannot refund payment with status: " + payment.getPaymentStatus());
                }
        }

        public java.io.ByteArrayInputStream exportPaymentsToCSV() {
                List<Payments> payments = paymentRepository.findAll();

                try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                                java.io.PrintWriter writer = new java.io.PrintWriter(out)) {

                        // Header
                        writer.println("Transaction ID, User, Event, Organizer, Amount, Status, Method, Date");

                        for (Payments payment : payments) {
                                String user = payment.getEventRegistration() != null
                                                && payment.getEventRegistration().getUser() != null
                                                                ? payment.getEventRegistration().getUser().getName()
                                                                : "N/A";
                                String event = payment.getEventRegistration() != null
                                                && payment.getEventRegistration().getEvent() != null
                                                                ? payment.getEventRegistration().getEvent().getTitle()
                                                                : "N/A";
                                String organizer = payment.getEventRegistration() != null
                                                && payment.getEventRegistration().getEvent() != null
                                                && payment.getEventRegistration().getEvent().getOrganizer() != null
                                                                ? payment.getEventRegistration().getEvent()
                                                                                .getOrganizer().getOrganization_name()
                                                                : "N/A";

                                writer.printf("%s, %s, %s, %s, %.2f, %s, %s, %s\n",
                                                payment.getTransactionUuid(),
                                                escapeSpecialCharacters(user),
                                                escapeSpecialCharacters(event),
                                                escapeSpecialCharacters(organizer),
                                                payment.getAmount() != null ? payment.getAmount() : 0.0,
                                                payment.getPaymentStatus(),
                                                payment.getMethod(),
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

        private AdminPaymentDTO mapToAdminPaymentDTO(Payments payment) {
                var builder = AdminPaymentDTO.builder()
                                .id(payment.getId())
                                .transactionId(payment.getTransactionUuid())
                                .amount(payment.getAmount() != null ? payment.getAmount() : 0.0)
                                .fee(payment.getFee() != null ? payment.getFee() : 0.0)
                                .netAmount(payment.getNetAmount() != null ? payment.getNetAmount() : 0.0)
                                .currency("NPR") // Assuming NPR as default based on phone numbers +977
                                .status(payment.getPaymentStatus())
                                .paymentMethod(payment.getMethod())
                                .paymentDate(payment.getTransactionDate())
                                .releasedDate(payment.getReleasedAt())
                                .releaseNotes(payment.getReleaseNotes())
                                .verifiedAt(payment.getVerifiedAt());

                if (payment.getVerifiedBy() != null) {
                        builder.verifiedBy(AdminPaymentDTO.UserInfo.builder()
                                        .id(payment.getVerifiedBy().getId())
                                        .name(payment.getVerifiedBy().getName())
                                        .build());
                }

                if (payment.getReleasedBy() != null) {
                        builder.releasedBy(AdminPaymentDTO.UserInfo.builder()
                                        .id(payment.getReleasedBy().getId())
                                        .name(payment.getReleasedBy().getName())
                                        .build());
                }

                if (payment.getEventRegistration() != null) {
                        var reg = payment.getEventRegistration();
                        var user = reg.getUser();
                        var event = reg.getEvent();

                        if (user != null) {
                                builder.user(AdminPaymentDTO.UserInfo.builder()
                                                .id(user.getId())
                                                .name(user.getName())
                                                .email(user.getEmail())
                                                .phone(user.getPhone())
                                                .build());
                        }

                        if (event != null) {
                                builder.event(AdminPaymentDTO.EventInfo.builder()
                                                .id(event.getId())
                                                .title(event.getTitle())
                                                .date(event.getDate() != null ? event.getDate().toString() : "")
                                                .location(event.getLocation())
                                                .build());

                                if (event.getOrganizer() != null) {
                                        var org = event.getOrganizer();
                                        builder.organizer(AdminPaymentDTO.OrganizerInfo.builder()
                                                        .id(org.getId())
                                                        .name(org.getUser().getName())
                                                        .organization(org.getOrganization_name())
                                                        .email(org.getUser().getEmail())
                                                        .build());
                                }
                        }

                        // Using eventParticipants size as participant count assumption, or default to 1
                        int participantCount = (reg.getEventParticipants() != null) ? reg.getEventParticipants().size()
                                        : 1;
                        builder.registration(AdminPaymentDTO.RegistrationInfo.builder()
                                        .id(reg.getId())
                                        .participants(participantCount)
                                        .bookingDate(reg.getRegistrationDate())
                                        .build());
                }

                return builder.build();
        }
}
