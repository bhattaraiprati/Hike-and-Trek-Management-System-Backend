package com.example.treksathi.repository;

import com.example.treksathi.enums.Approval_status;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Integer>, JpaSpecificationExecutor<Organizer> {
    // Change from findByUserID to findByUserId
    Optional<Organizer> findByUserId(int id);

    Organizer findByUser(User user);

    // Admin verification methods
    List<Organizer> findByApprovalStatus(Approval_status status);

    long countByApprovalStatus(Approval_status status);
}
