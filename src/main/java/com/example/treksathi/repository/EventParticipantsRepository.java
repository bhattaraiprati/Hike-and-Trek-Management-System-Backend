package com.example.treksathi.repository;

import com.example.treksathi.model.EventParticipants;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventParticipantsRepository extends JpaRepository<EventParticipants, Integer> {
}
