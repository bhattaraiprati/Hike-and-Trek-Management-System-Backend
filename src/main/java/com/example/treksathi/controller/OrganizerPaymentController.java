package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.IOrganizerPaymentService;
import com.example.treksathi.dto.organizer.PaymentDashboardDTO;
import com.example.treksathi.dto.organizer.PaymentFilterDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizer/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organizer Payment APIs", description = "APIs for organizer payment management and dashboard")
public class OrganizerPaymentController {

    private final IOrganizerPaymentService organizerPaymentService;

    @GetMapping("/dashboard/{organizerId}")
    public ResponseEntity<PaymentDashboardDTO> getPaymentDashboard(
            @PathVariable Integer organizerId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false, defaultValue = "ALL") String paymentMethod
    ) {
            log.info("Fetching payment dashboard for organizer: {}", organizerId);
        
        PaymentFilterDTO filters = new PaymentFilterDTO();
        
        // Set date range
        if (fromDate != null || toDate != null) {
            PaymentFilterDTO.DateRange dateRange = new PaymentFilterDTO.DateRange();
            if (fromDate != null) {
                dateRange.setFrom(java.time.LocalDate.parse(fromDate));
            } else {
                // Default to 1 month ago
                dateRange.setFrom(java.time.LocalDate.now().minusMonths(1));
            }
            if (toDate != null) {
                dateRange.setTo(java.time.LocalDate.parse(toDate));
            } else {
                dateRange.setTo(java.time.LocalDate.now());
            }
            filters.setDateRange(dateRange);
        } else {
            // Default date range: last month
            PaymentFilterDTO.DateRange dateRange = new PaymentFilterDTO.DateRange();
            dateRange.setFrom(java.time.LocalDate.now().minusMonths(1));
            dateRange.setTo(java.time.LocalDate.now());
            filters.setDateRange(dateRange);
        }
        
        filters.setStatus(status != null ? status : "ALL");
        filters.setEventId(eventId);
        filters.setPaymentMethod(paymentMethod != null ? paymentMethod : "ALL");
        
        PaymentDashboardDTO dashboard = organizerPaymentService.getPaymentDashboard(organizerId, filters);
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/dashboard/{organizerId}")
    public ResponseEntity<PaymentDashboardDTO> getPaymentDashboardWithFilters(
            @PathVariable Integer organizerId,
            @RequestBody(required = false) PaymentFilterDTO filters
    ) {
        log.info("Fetching payment dashboard for organizer: {} with filters", organizerId);
        
        // Set default filters if not provided
        if (filters == null) {
            filters = new PaymentFilterDTO();
        }
        
        if (filters.getDateRange() == null) {
            PaymentFilterDTO.DateRange dateRange = new PaymentFilterDTO.DateRange();
            dateRange.setFrom(java.time.LocalDate.now().minusMonths(1));
            dateRange.setTo(java.time.LocalDate.now());
            filters.setDateRange(dateRange);
        }
        
        if (filters.getStatus() == null) {
            filters.setStatus("ALL");
        }
        
        if (filters.getPaymentMethod() == null) {
            filters.setPaymentMethod("ALL");
        }
        
        PaymentDashboardDTO dashboard = organizerPaymentService.getPaymentDashboard(organizerId, filters);
        return ResponseEntity.ok(dashboard);
    }
}

