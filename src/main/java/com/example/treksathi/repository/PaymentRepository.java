package com.example.treksathi.repository;

import com.example.treksathi.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payments,Integer> {

    Optional<Payments> findByTransactionUuid(String id);
}
