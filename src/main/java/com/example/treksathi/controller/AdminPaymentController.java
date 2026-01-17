package com.example.treksathi.controller;

import com.example.treksathi.dto.payment.AdminPaymentDTO;
import com.example.treksathi.dto.payment.OrganizerBalanceDTO;
import com.example.treksathi.dto.payment.PaymentStatsDTO;
import com.example.treksathi.model.User;
import com.example.treksathi.service.AdminPaymentService;
import com.example.treksathi.service.UserServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Payment Management", description = "APIs for managing payments (admin only)")
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;
    private final UserServices userServices;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get all payments", description = "Get paginated list of payments with filtering", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<AdminPaymentDTO>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "ALL") String method,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer organizerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(adminPaymentService.getAllPayments(page, size, status, method, search, organizerId,
                startDate, endDate));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get payment stats", description = "Get statistics for dashboard", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PaymentStatsDTO> getPaymentStats() {
        return ResponseEntity.ok(adminPaymentService.getPaymentStats());
    }

    @GetMapping("/balances")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get organizer balances", description = "Get balance information for all organizers", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<OrganizerBalanceDTO>> getOrganizerBalances() {
        return ResponseEntity.ok(adminPaymentService.getOrganizerBalances());
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Verify payment", description = "Mark payment as completed/verified", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AdminPaymentDTO> verifyPayment(@PathVariable int id) {
        int adminId = getAuthenticatedUserId();
        return ResponseEntity.ok(adminPaymentService.verifyPayment(id, adminId));
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Release payment", description = "Mark payment as released to organizer", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AdminPaymentDTO> releasePayment(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {
        int adminId = getAuthenticatedUserId();
        String notes = body.getOrDefault("notes", "");
        return ResponseEntity.ok(adminPaymentService.releasePayment(id, adminId, notes));
    }

    @PostMapping("/release/bulk")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Bulk release payments", description = "Release multiple payments at once", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> bulkRelease(@RequestBody List<Integer> paymentIds) {
        adminPaymentService.bulkRelease(paymentIds, getAuthenticatedUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Refund a payment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AdminPaymentDTO> refundPayment(@PathVariable int id) {
        return ResponseEntity.ok(adminPaymentService.refundPayment(id, getAuthenticatedUserId()));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Export payments to CSV", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<org.springframework.core.io.InputStreamResource> exportPayments() {
        var stream = adminPaymentService.exportPaymentsToCSV();

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payments.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/csv"))
                .body(new org.springframework.core.io.InputStreamResource(stream));
    }

    private int getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userServices.findByEmail(email).map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
