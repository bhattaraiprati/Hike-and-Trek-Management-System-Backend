package com.example.treksathi.service;

import com.example.treksathi.Interfaces.IAdminOrganizerService;
import com.example.treksathi.dto.admin.AdminOrganizerStatsDTO;
import com.example.treksathi.dto.admin.OrganizerVerificationDetailDTO;
import com.example.treksathi.dto.admin.OrganizerVerificationListDTO;
import com.example.treksathi.enums.Approval_status;
import com.example.treksathi.exception.NotFoundException;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.model.User;
import com.example.treksathi.repository.EventRepository;
import com.example.treksathi.repository.OrganizerRepository;
import com.example.treksathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrganizerService implements IAdminOrganizerService {

    private final OrganizerRepository organizerRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<OrganizerVerificationListDTO> getAllOrganizers(Approval_status status, String search) {
        List<Organizer> organizers;

        if (status != null) {
            organizers = organizerRepository.findByApprovalStatus(status);
        } else {
            organizers = organizerRepository.findAll();
        }

        // Apply search filter if provided
        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase();
            organizers = organizers.stream()
                    .filter(org -> org.getOrganization_name().toLowerCase().contains(searchLower) ||
                            org.getContact_person().toLowerCase().contains(searchLower) ||
                            org.getUser().getEmail().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        return organizers.stream()
                .map(this::mapToListDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrganizerVerificationDetailDTO getOrganizerDetails(int organizerId) {
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new NotFoundException("Organizer not found with id: " + organizerId));

        return mapToDetailDTO(organizer);
    }

    @Override
    @Transactional
    public OrganizerVerificationListDTO approveOrganizer(int organizerId, int adminUserId) {
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new NotFoundException("Organizer not found with id: " + organizerId));

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("Admin user not found with id: " + adminUserId));

        organizer.setApprovalStatus(Approval_status.SUCCESS);
        organizer.setVerified_by(adminUser);
        organizer.setVerified_on(LocalDateTime.now());

        organizer = organizerRepository.save(organizer);

        log.info("Organizer {} approved by admin {}", organizerId, adminUserId);

        return mapToListDTO(organizer);
    }

    @Override
    @Transactional
    public OrganizerVerificationListDTO rejectOrganizer(int organizerId, int adminUserId, String reason) {
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new NotFoundException("Organizer not found with id: " + organizerId));

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("Admin user not found with id: " + adminUserId));

        organizer.setApprovalStatus(Approval_status.DECLINE);
        organizer.setVerified_by(adminUser);
        organizer.setVerified_on(LocalDateTime.now());

        organizer = organizerRepository.save(organizer);

        log.info("Organizer {} rejected by admin {} with reason: {}", organizerId, adminUserId, reason);

        return mapToListDTO(organizer);
    }

    @Override
    public AdminOrganizerStatsDTO getStats() {
        AdminOrganizerStatsDTO stats = new AdminOrganizerStatsDTO();

        stats.setTotalOrganizers(organizerRepository.count());
        stats.setPendingCount(organizerRepository.countByApprovalStatus(Approval_status.PENDING));
        stats.setApprovedCount(organizerRepository.countByApprovalStatus(Approval_status.SUCCESS));
        stats.setRejectedCount(organizerRepository.countByApprovalStatus(Approval_status.DECLINE));

        return stats;
    }

    private OrganizerVerificationListDTO mapToListDTO(Organizer organizer) {
        OrganizerVerificationListDTO dto = new OrganizerVerificationListDTO();
        User user = organizer.getUser();

        dto.setId(organizer.getId());
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getName());
        dto.setProfileImage(user.getProfileImage());

        dto.setOrganizationName(organizer.getOrganization_name());
        dto.setContactPerson(organizer.getContact_person());
        dto.setAddress(organizer.getAddress());
        dto.setPhone(organizer.getPhone());
        dto.setDocumentUrl(organizer.getDocument_url());

        dto.setApprovalStatus(organizer.getApprovalStatus());
        dto.setVerifiedByName(organizer.getVerified_by() != null ? organizer.getVerified_by().getName() : null);
        dto.setVerifiedOn(organizer.getVerified_on());
        dto.setCreatedAt(organizer.getCreatedAt());

        // Get events count
        int eventsCount = eventRepository.countByOrganizerId(organizer.getId());
        dto.setEventsCount(eventsCount);

        return dto;
    }

    private OrganizerVerificationDetailDTO mapToDetailDTO(Organizer organizer) {
        OrganizerVerificationDetailDTO dto = new OrganizerVerificationDetailDTO();
        User user = organizer.getUser();

        dto.setId(organizer.getId());
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getName());
        dto.setProfileImage(user.getProfileImage());

        dto.setOrganizationName(organizer.getOrganization_name());
        dto.setContactPerson(organizer.getContact_person());
        dto.setAddress(organizer.getAddress());
        dto.setPhone(organizer.getPhone());
        dto.setCoverImage(organizer.getCover_image());
        dto.setDocumentUrl(organizer.getDocument_url());
        dto.setAbout(organizer.getAbout());

        dto.setApprovalStatus(organizer.getApprovalStatus());
        dto.setVerifiedBy(null); // Will set below
        dto.setVerifiedOn(organizer.getVerified_on());
        dto.setCreatedAt(organizer.getCreatedAt());

        // Map verified by user
        if (organizer.getVerified_by() != null) {
            OrganizerVerificationDetailDTO.VerifiedByDTO verifiedBy = new OrganizerVerificationDetailDTO.VerifiedByDTO();
            verifiedBy.setId(organizer.getVerified_by().getId());
            verifiedBy.setEmail(organizer.getVerified_by().getEmail());
            verifiedBy.setFullName(organizer.getVerified_by().getName());
            dto.setVerifiedBy(verifiedBy);
        }

        // Get events count
        int eventsCount = eventRepository.countByOrganizerId(organizer.getId());
        dto.setEventsCount(eventsCount);

        return dto;
    }
}
