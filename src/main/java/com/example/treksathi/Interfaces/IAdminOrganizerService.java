package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.admin.AdminOrganizerStatsDTO;
import com.example.treksathi.dto.admin.OrganizerVerificationDetailDTO;
import com.example.treksathi.dto.admin.OrganizerVerificationListDTO;
import com.example.treksathi.enums.Approval_status;

import java.util.List;

public interface IAdminOrganizerService {

    /**
     * Get all organizers with optional status filter and search
     */
    List<OrganizerVerificationListDTO> getAllOrganizers(Approval_status status, String search);

    /**
     * Get organizer details by ID
     */
    OrganizerVerificationDetailDTO getOrganizerDetails(int organizerId);

    /**
     * Approve an organizer
     */
    OrganizerVerificationListDTO approveOrganizer(int organizerId, int adminUserId);

    /**
     * Reject an organizer with reason
     */
    OrganizerVerificationListDTO rejectOrganizer(int organizerId, int adminUserId, String reason);

    /**
     * Get verification statistics
     */
    AdminOrganizerStatsDTO getStats();
}
