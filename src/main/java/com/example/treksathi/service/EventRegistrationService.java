package com.example.treksathi.service;

import com.example.treksathi.enums.EventRegistrationStatus;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.exception.EventNotFoundException;
import com.example.treksathi.mapper.BookingResponseMapper;
import com.example.treksathi.mapper.EventRegistrationMapper;
import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.record.*;
import com.example.treksathi.repository.EventRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventRegistrationService {

    private  final EventRegistrationRepository eventRegistrationRepository;
    private final EventRegistrationMapper mapper;

    private final BookingResponseMapper bookingMapper;

    public EventRegistrationResponse getRegistrationDetailsById(int id){
        EventRegistration reg = eventRegistrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
        return mapper.toResponse(reg);
    }

    public List<BookingResponseRecord> getAllEventsByUserId(int id, List<EventRegistrationStatus> status){
        List<EventRegistration> reg = eventRegistrationRepository.findByUserIdAndStatus(id, status).orElseThrow(null);

        return reg.stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    public List<UpcommingEventRecord> getAllUpcomingEvent(int id){
        List<UpcommingEventRecord> registrations = eventRegistrationRepository.findActiveEventByUserId(id, EventStatus.ACTIVE)
                .orElseThrow(()-> new EventNotFoundException("Event is Not Found For this userId"+ id));

         return  registrations;
    }
}
