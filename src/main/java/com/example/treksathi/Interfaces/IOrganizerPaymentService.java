package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.organizer.PaymentDashboardDTO;
import com.example.treksathi.dto.organizer.PaymentFilterDTO;

public interface IOrganizerPaymentService {
    PaymentDashboardDTO getPaymentDashboard(Integer organizerId, PaymentFilterDTO filters);

    org.springframework.data.domain.Page<com.example.treksathi.dto.organizer.ParticipantPaymentDTO> getPayments(
            Integer organizerId, PaymentFilterDTO filters, int page, int size);

    java.io.ByteArrayInputStream exportPaymentsToCSV(Integer organizerId);
}
