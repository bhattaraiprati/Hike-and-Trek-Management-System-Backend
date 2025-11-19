package com.example.treksathi.repository;

import com.example.treksathi.model.Organizer;
import com.example.treksathi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Integer> {
    Organizer findByUser(Optional<User> user);

//    Optional<Organizer> findByEmail(String email);
}
