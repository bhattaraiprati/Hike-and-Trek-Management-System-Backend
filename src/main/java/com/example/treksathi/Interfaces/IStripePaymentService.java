package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.events.EventRegisterDTO;

import com.example.treksathi.dto.payments.StripePaymentResponse;
import com.example.treksathi.model.EventRegistration;

public interface IStripePaymentService {
    StripePaymentResponse createCheckoutSession(EventRegisterDTO eventRegisterDTO) throws Exception;
    EventRegistration handleSuccessfulPayment(String sessionId) throws Exception;
}
