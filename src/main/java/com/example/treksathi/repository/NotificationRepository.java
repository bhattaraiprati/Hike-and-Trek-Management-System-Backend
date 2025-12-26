package com.example.treksathi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.treksathi.model.Notification;
import com.example.treksathi.model.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

}

