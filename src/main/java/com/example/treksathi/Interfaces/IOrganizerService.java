package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.organizer.OrganizerDashboardDTO;
import com.example.treksathi.dto.organizer.OrganizerProfileDTO;
import com.example.treksathi.dto.organizer.OrganizerRegistrationDTO;
import com.example.treksathi.model.Organizer;

public interface IOrganizerService {
    Organizer registerOrganizer(OrganizerRegistrationDTO dto);

    OrganizerProfileDTO getOrganizerProfile(int userId);

    OrganizerProfileDTO updateOrganizerProfile(int userId, OrganizerProfileDTO profileDTO);

    OrganizerDashboardDTO getOrganizerDashboard(int userId);
}
