    package com.example.treksathi.mapper;

    import com.example.treksathi.model.EventParticipants;
    import com.example.treksathi.model.EventRegistration;
    import com.example.treksathi.model.Payments;
    import com.example.treksathi.record.BookingResponseRecord;
    import com.example.treksathi.record.EventRegistrationResponse;
    import org.springframework.stereotype.Component;

    import java.util.List;

    @Component
    public class BookingResponseMapper {

        public BookingResponseRecord toResponse(EventRegistration reg) {

            Payments payments = reg.getPayments();
            var event = reg.getEvent();
            var organizer = event.getOrganizer();

            var eventDetails = new BookingResponseRecord.EventDetails(
                    event.getId(),
                    event.getTitle(),
                    event.getLocation(),
                    event.getDate(),
                    event.getDurationDays(),
                    event.getStatus().name(),
                    event.getDifficultyLevel().name(),
                    event.getPrice(),
                    event.getBannerImageUrl(),
                    event.getIncludedServices(),
                    event.getRequirements(),
                    event.getMeetingPoint(),
                    event.getMeetingTime(),
                    new BookingResponseRecord.OrganizerInfo(
                            organizer.getOrganization_name(),
                            organizer.getPhone()


                    )
            );

            int totalParticipants = reg.getEventParticipants().size();
            var participantDetails = new BookingResponseRecord.ParticipantDetails(totalParticipants);

            // Payment Details
            var paymentDetails = (payments != null)
                    ? new BookingResponseRecord.PaymentDetails(
                    payments.getMethod().name().toLowerCase(),
                    payments.getAmount(),
                    payments.getPaymentStatus().name().toLowerCase()
            )
                    : new BookingResponseRecord.PaymentDetails(
                    "pending",
                    0.0,
                    "unpaid"
            );

            return new BookingResponseRecord(
                    reg.getId(),
                    reg.getRegistrationDate(),
                    reg.getContactName(),
                    reg.getContact(),
                    eventDetails,
                    List.of(participantDetails),  // List with single object containing count
                    paymentDetails
            );


        }
    }
