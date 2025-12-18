package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.organizer.OrganizerRegistrationDTO;
import com.example.treksathi.model.Organizer;

public interface IOrganizerService {
    Organizer registerOrganizer(OrganizerRegistrationDTO dto);

}
