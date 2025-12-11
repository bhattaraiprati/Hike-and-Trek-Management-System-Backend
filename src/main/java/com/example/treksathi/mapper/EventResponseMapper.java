package com.example.treksathi.mapper;

import com.example.treksathi.model.Event;
import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.model.Payments;
import com.example.treksathi.record.EventDetailsOrganizerRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventResponseMapper {

    public EventDetailsOrganizerRecord toEventDetailsOrganizerRecord(Event event) {
        var organizer = event.getOrganizer();
        List<EventDetailsOrganizerRecord.EventRegistrationInfo> eventRegistrations = event.getEventRegistration().stream()
                .map(reg -> {
                    Payments payments = reg.getPayments();
                    EventDetailsOrganizerRecord.PaymentInfo paymentInfo = null;
                    if (payments != null) {
                        paymentInfo = new EventDetailsOrganizerRecord.PaymentInfo(
                                payments.getId(),
                                payments.getAmount(),
                                payments.getMethod().name(),
                                payments.getPaymentStatus().name(),
                                payments.getTransactionDate()
                        );
                    }

                    List<EventDetailsOrganizerRecord.ParticipantInfo> participantInfos = reg.getEventParticipants().stream()
                            .map(participant -> new EventDetailsOrganizerRecord.ParticipantInfo(
                                    participant.getId(),
                                    participant.getName(),
                                    participant.getGender().name(),
                                    participant.getNationality(),
                                    participant.getAttendanceStatus()
                            ))
                            .collect(Collectors.toList());
                    return new EventDetailsOrganizerRecord.EventRegistrationInfo(
                            reg.getId(),
                            reg.getRegistrationDate(),
                            reg.getContact(),
                            reg.getContactName(),
                            reg.getEmail(),
                            reg.getStatus().name(),
                            paymentInfo,
                            participantInfos
                    );
                })
                .collect(Collectors.toList());



        return new EventDetailsOrganizerRecord(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getDate(),
                event.getDurationDays(),
                event.getDifficultyLevel().name(),
                event.getPrice(),
                event.getBannerImageUrl(),
                event.getMaxParticipants(),
                event.getMeetingPoint(),
                event.getMeetingTime(),
                organizer.getContact_person(),
                organizer.getUser().getEmail(),
                List.of(event.getIncludedServices().toString()),
                List.of(event.getRequirements().toString()),
                event.getStatus().name(),
                event.getCreatedAt(),
                eventRegistrations

        );
    }
}
