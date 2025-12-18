package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.pagination.PaginatedResponseDTO;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.record.EventCardResponse;
import com.example.treksathi.record.EventResponseRecord;
import com.example.treksathi.record.OrganizerRecord;

public interface IEventService {
    PaginatedResponseDTO<EventCardResponse> getAllEvents(int page, int size);
    EventResponseRecord getEventById(int id);

}
