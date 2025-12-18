package com.example.treksathi.Interfaces;

import com.example.treksathi.enums.EventRegistrationStatus;
import com.example.treksathi.record.BookingResponseRecord;
import com.example.treksathi.record.EventRegistrationResponse;
import com.example.treksathi.record.UpcommingEventRecord;

import java.util.List;

public interface  IEventRegistrationService {
    EventRegistrationResponse getRegistrationDetailsById(int id);
    List<BookingResponseRecord> getAllEventsByUserId(int id, List<EventRegistrationStatus> status);
    List<UpcommingEventRecord> getAllUpcomingEvent(int id);
}
