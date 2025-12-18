package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.events.EventRegisterDTO;
import com.example.treksathi.model.EventRegistration;

public interface IPaymentGatewayService {
    Object initiateEsewaPayment(EventRegisterDTO eventRegisterDTO);
    EventRegistration createEvent(EventRegisterDTO eventRegisterDTO);
    EventRegistration verifyAndConfirmPayment(String transactionUuid) throws Exception;
    EventRegistration getRegistrationByTransactionUuid(String transactionUuid);

}
