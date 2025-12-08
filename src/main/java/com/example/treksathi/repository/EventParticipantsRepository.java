package com.example.treksathi.repository;

import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.EventParticipants;
import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.record.EventCardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventParticipantsRepository extends JpaRepository<EventParticipants, Integer> {

}
