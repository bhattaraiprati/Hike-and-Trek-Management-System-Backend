package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.organizer.PaymentDashboardDTO;
import com.example.treksathi.dto.organizer.PaymentFilterDTO;

public interface IOrganizerPaymentService {
    PaymentDashboardDTO getPaymentDashboard(Integer organizerId, PaymentFilterDTO filters);
}

