package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.IOrganizerPaymentService;
import com.example.treksathi.dto.organizer.PaymentDashboardDTO;
import com.example.treksathi.dto.organizer.PaymentFilterDTO;
import com.example.treksathi.dto.organizer.PaymentSummaryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizerPaymentController.class)
@DisplayName("OrganizerPaymentController Unit Tests")
class OrganizerPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IOrganizerPaymentService organizerPaymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentDashboardDTO mockDashboardDTO;
    private PaymentSummaryDTO mockSummaryDTO;

    @BeforeEach
    void setUp() {
        // Setup mock summary
        mockSummaryDTO = PaymentSummaryDTO.builder()
                .totalIncome(1000.0)
                .completedPayments(10)
                .pendingPayments(2)
                .refundedPayments(1)
                .monthlyGrowth(15.5)
                .currency("$")
                .totalParticipants(25)
                .averagePayment(100.0)
                .platformFee(10)
                .build();

        // Setup mock dashboard
        mockDashboardDTO = PaymentDashboardDTO.builder()
                .summary(mockSummaryDTO)
                .events(Collections.emptyList())
                .participantPayments(Collections.emptyList())
                .recentPayments(Collections.emptyList())
                .revenueChart(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("GET /dashboard/{organizerId} - Should return payment dashboard successfully")
    void testGetPaymentDashboard_Success() throws Exception {
        // Given
        Integer organizerId = 1;
        when(organizerPaymentService.getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class)))
                .thenReturn(mockDashboardDTO);

        // When/Then
        mockMvc.perform(get("/organizer/payments/dashboard/{organizerId}", organizerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalIncome").value(1000.0))
                .andExpect(jsonPath("$.summary.completedPayments").value(10))
                .andExpect(jsonPath("$.summary.pendingPayments").value(2))
                .andExpect(jsonPath("$.summary.currency").value("$"));

        verify(organizerPaymentService, times(1))
                .getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class));
    }

    @Test
    @DisplayName("GET /dashboard/{organizerId} - Should handle query parameters correctly")
    void testGetPaymentDashboard_WithQueryParameters() throws Exception {
        // Given
        Integer organizerId = 1;
        String fromDate = "2025-01-01";
        String toDate = "2025-12-31";
        String status = "COMPLETED";
        Integer eventId = 5;
        String paymentMethod = "CREDIT_CARD";

        when(organizerPaymentService.getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class)))
                .thenReturn(mockDashboardDTO);

        // When/Then
        mockMvc.perform(get("/organizer/payments/dashboard/{organizerId}", organizerId)
                        .param("fromDate", fromDate)
                        .param("toDate", toDate)
                        .param("status", status)
                        .param("eventId", String.valueOf(eventId))
                        .param("paymentMethod", paymentMethod)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(organizerPaymentService, times(1))
                .getPaymentDashboard(eq(organizerId), argThat(filter -> {
                    assert filter != null;
                    assert filter.getDateRange() != null;
                    assert filter.getStatus().equals(status);
                    assert filter.getEventId().equals(eventId);
                    assert filter.getPaymentMethod().equals(paymentMethod);
                    return true;
                }));
    }

    @Test
    @DisplayName("GET /dashboard/{organizerId} - Should set default date range when not provided")
    void testGetPaymentDashboard_DefaultDateRange() throws Exception {
        // Given
        Integer organizerId = 1;

        when(organizerPaymentService.getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class)))
                .thenReturn(mockDashboardDTO);

        // When/Then
        mockMvc.perform(get("/organizer/payments/dashboard/{organizerId}", organizerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(organizerPaymentService, times(1))
                .getPaymentDashboard(eq(organizerId), argThat(filter -> {
                    assert filter != null;
                    assert filter.getDateRange() != null;
                    assert filter.getDateRange().getFrom() != null;
                    assert filter.getDateRange().getTo() != null;
                    return true;
                }));
    }

    @Test
    @DisplayName("GET /dashboard/{organizerId} - Should set default status to ALL when not provided")
    void testGetPaymentDashboard_DefaultStatus() throws Exception {
        // Given
        Integer organizerId = 1;

        when(organizerPaymentService.getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class)))
                .thenReturn(mockDashboardDTO);

        // When/Then
        mockMvc.perform(get("/organizer/payments/dashboard/{organizerId}", organizerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(organizerPaymentService, times(1))
                .getPaymentDashboard(eq(organizerId), argThat(filter -> {
                    assert filter != null;
                    assert filter.getStatus().equals("ALL");
                    return true;
                }));
    }

    @Test
    @DisplayName("POST /dashboard/{organizerId} - Should return payment dashboard with filters")
    void testGetPaymentDashboardWithFilters_Success() throws Exception {
        // Given
        Integer organizerId = 1;
        PaymentFilterDTO filters = new PaymentFilterDTO();
        PaymentFilterDTO.DateRange dateRange = new PaymentFilterDTO.DateRange();
        dateRange.setFrom(LocalDate.now().minusMonths(1));
        dateRange.setTo(LocalDate.now());
        filters.setDateRange(dateRange);
        filters.setStatus("COMPLETED");
        filters.setEventId(1);
        filters.setPaymentMethod("CREDIT_CARD");

        when(organizerPaymentService.getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class)))
                .thenReturn(mockDashboardDTO);

        // When/Then
        mockMvc.perform(post("/organizer/payments/dashboard/{organizerId}", organizerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").exists());

        verify(organizerPaymentService, times(1))
                .getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class));
    }

    @Test
    @DisplayName("POST /dashboard/{organizerId} - Should handle null filters and set defaults")
    void testGetPaymentDashboardWithFilters_NullFilters() throws Exception {
        // Given
        Integer organizerId = 1;

        when(organizerPaymentService.getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class)))
                .thenReturn(mockDashboardDTO);

        // When/Then
        mockMvc.perform(post("/organizer/payments/dashboard/{organizerId}", organizerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isOk());

        verify(organizerPaymentService, times(1))
                .getPaymentDashboard(eq(organizerId), argThat(filter -> {
                    assert filter != null;
                    assert filter.getDateRange() != null;
                    assert filter.getStatus().equals("ALL");
                    assert filter.getPaymentMethod().equals("ALL");
                    return true;
                }));
    }

    @Test
    @DisplayName("POST /dashboard/{organizerId} - Should set default date range when missing")
    void testGetPaymentDashboardWithFilters_DefaultDateRange() throws Exception {
        // Given
        Integer organizerId = 1;
        PaymentFilterDTO filters = new PaymentFilterDTO();
        filters.setStatus("PENDING");
        // No date range set

        when(organizerPaymentService.getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class)))
                .thenReturn(mockDashboardDTO);

        // When/Then
        mockMvc.perform(post("/organizer/payments/dashboard/{organizerId}", organizerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk());

        verify(organizerPaymentService, times(1))
                .getPaymentDashboard(eq(organizerId), argThat(filter -> {
                    assert filter != null;
                    assert filter.getDateRange() != null;
                    assert filter.getDateRange().getFrom() != null;
                    assert filter.getDateRange().getTo() != null;
                    return true;
                }));
    }

    @Test
    @DisplayName("POST /dashboard/{organizerId} - Should set default status when missing")
    void testGetPaymentDashboardWithFilters_DefaultStatus() throws Exception {
        // Given
        Integer organizerId = 1;
        PaymentFilterDTO filters = new PaymentFilterDTO();
        PaymentFilterDTO.DateRange dateRange = new PaymentFilterDTO.DateRange();
        dateRange.setFrom(LocalDate.now().minusMonths(1));
        dateRange.setTo(LocalDate.now());
        filters.setDateRange(dateRange);
        // No status set

        when(organizerPaymentService.getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class)))
                .thenReturn(mockDashboardDTO);

        // When/Then
        mockMvc.perform(post("/organizer/payments/dashboard/{organizerId}", organizerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk());

        verify(organizerPaymentService, times(1))
                .getPaymentDashboard(eq(organizerId), argThat(filter -> {
                    assert filter != null;
                    assert filter.getStatus().equals("ALL");
                    return true;
                }));
    }

    @Test
    @DisplayName("POST /dashboard/{organizerId} - Should set default payment method when missing")
    void testGetPaymentDashboardWithFilters_DefaultPaymentMethod() throws Exception {
        // Given
        Integer organizerId = 1;
        PaymentFilterDTO filters = new PaymentFilterDTO();
        PaymentFilterDTO.DateRange dateRange = new PaymentFilterDTO.DateRange();
        dateRange.setFrom(LocalDate.now().minusMonths(1));
        dateRange.setTo(LocalDate.now());
        filters.setDateRange(dateRange);
        filters.setStatus("COMPLETED");
        // No payment method set

        when(organizerPaymentService.getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class)))
                .thenReturn(mockDashboardDTO);

        // When/Then
        mockMvc.perform(post("/organizer/payments/dashboard/{organizerId}", organizerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filters)))
                .andExpect(status().isOk());

        verify(organizerPaymentService, times(1))
                .getPaymentDashboard(eq(organizerId), argThat(filter -> {
                    assert filter != null;
                    assert filter.getPaymentMethod().equals("ALL");
                    return true;
                }));
    }

    @Test
    @DisplayName("GET /dashboard/{organizerId} - Should handle partial date range")
    void testGetPaymentDashboard_PartialDateRange() throws Exception {
        // Given
        Integer organizerId = 1;
        String fromDate = "2025-01-01";
        // toDate not provided

        when(organizerPaymentService.getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class)))
                .thenReturn(mockDashboardDTO);

        // When/Then
        mockMvc.perform(get("/organizer/payments/dashboard/{organizerId}", organizerId)
                        .param("fromDate", fromDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(organizerPaymentService, times(1))
                .getPaymentDashboard(eq(organizerId), argThat(filter -> {
                    assert filter != null;
                    assert filter.getDateRange() != null;
                    assert filter.getDateRange().getFrom() != null;
                    assert filter.getDateRange().getTo() != null; // Should default to today
                    return true;
                }));
    }

    @Test
    @DisplayName("GET /dashboard/{organizerId} - Should handle invalid organizer ID")
    void testGetPaymentDashboard_InvalidOrganizerId() throws Exception {
        // Given
        Integer organizerId = -1;

        when(organizerPaymentService.getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class)))
                .thenThrow(new RuntimeException("Organizer not found"));

        // When/Then
        mockMvc.perform(get("/organizer/payments/dashboard/{organizerId}", organizerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(organizerPaymentService, times(1))
                .getPaymentDashboard(eq(organizerId), any(PaymentFilterDTO.class));
    }
}

