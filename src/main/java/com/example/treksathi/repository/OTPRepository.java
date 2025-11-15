package com.example.treksathi.repository;

import com.example.treksathi.model.OTP;
import com.example.treksathi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTP, Integer> {


    Optional<OTP> findByUser(User user);
}
