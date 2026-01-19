package com.example.treksathi.service;

import com.example.treksathi.dto.PlatformStatsDTO;
import com.example.treksathi.enums.Approval_status;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.repository.EventRepository;
import com.example.treksathi.repository.OrganizerRepository;
import com.example.treksathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicStatsService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;

    public PlatformStatsDTO getPlatformStats() {
        long totalTrails = eventRepository.countByStatus(EventStatus.ACTIVE);
        long communityMembers = userRepository.count();
        long verifiedOrganizers = organizerRepository.countByApprovalStatus(Approval_status.SUCCESS);

        return PlatformStatsDTO.builder()
                .totalTrails(totalTrails)
                .communityMembers(communityMembers)
                .verifiedOrganizers(verifiedOrganizers)
                .build();
    }
}
