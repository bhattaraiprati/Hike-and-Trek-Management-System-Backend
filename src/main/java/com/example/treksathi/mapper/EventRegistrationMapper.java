package com.example.treksathi.mapper;

import com.example.treksathi.model.EventParticipants;
import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.model.Payments;
import com.example.treksathi.record.EventRegistrationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventRegistrationMapper {

    public EventRegistrationResponse toResponse(EventRegistration reg){

        Payments pay = reg.getPayments();
        if(pay == null ){
            throw  new IllegalStateException("Payment information is missing");
        }

        var event = reg.getEvent();
        var organizer = event.getOrganizer();

        var eventDetails = new EventRegistrationResponse.EventDetails(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getDate(),
                event.getDurationDays(),
                event.getDifficultyLevel().name(),
                event.getPrice(),
                event.getBannerImageUrl(),
                event.getIncludedServices(),
                event.getRequirements(),
                event.getMeetingPoint(),
                event.getMeetingTime(),
                new EventRegistrationResponse.OrganizerInfo(
                        organizer.getOrganization_name(),
                        organizer.getContact_person(),
                        organizer.getUser().getEmail(),
                        organizer.getPhone()
                )
        );

        List<EventRegistrationResponse.ParticipantDetails> participants = reg.getEventParticipants().stream()
                .map(this::mapParticipant)
                .toList();

        String transactionId = pay.getTransactionReference() != null
                ? pay.getTransactionReference()
                : pay.getTransactionUuid();

        var paymentInfo = new EventRegistrationResponse.PaymentDetails(
                pay.getMethod().name().toLowerCase(),
                transactionId,
                pay.getAmount(),
                pay.getTransactionDate(),
                pay.getPaymentStatus().name().toLowerCase()
        );

        return new EventRegistrationResponse(
                reg.getId(),
                reg.getRegistrationDate(),
                reg.getStatus().name(),
                pay.getAmount(),
                reg.getContactName(),
                reg.getContact(),
                reg.getEmail(),
                eventDetails,
                participants,
                paymentInfo
        );

    }


    private EventRegistrationResponse.ParticipantDetails mapParticipant(EventParticipants p) {
        String title = p.getGender() == com.example.treksathi.enums.Gender.MALE ? "Mr" : "Mrs";
        return new EventRegistrationResponse.ParticipantDetails(
                title,
                p.getName(),
                p.getNationality(),
                p.getGender().name()
        );
    }
}
