package com.example.treksathi.service;

import com.example.treksathi.mapper.BookingResponseMapper;
import com.example.treksathi.mapper.EventRegistrationMapper;
import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.record.BookingResponseRecord;
import com.example.treksathi.record.EventRegistrationResponse;
import com.example.treksathi.record.EventResponseRecord;
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

    public List<BookingResponseRecord> getALlEventsByUserId(int id){
        List<EventRegistration> reg = eventRegistrationRepository.findByUserId(id).orElseThrow(null);

        return reg.stream()
                .map(bookingMapper::toResponse)
                .toList();
    }
}
