package com.example.treksathi.repository;

import com.example.treksathi.model.Organizer;
import com.example.treksathi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Integer> {
    // Change from findByUserID to findByUserId
    Optional<Organizer> findByUserId(int id);

    Organizer findByUser(User user);
}