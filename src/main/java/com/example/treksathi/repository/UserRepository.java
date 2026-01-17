package com.example.treksathi.repository;

import com.example.treksathi.enums.AuthProvidertype;
import com.example.treksathi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Integer>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    Optional<User> findByProviderIdAndProviderType(String providerId, AuthProvidertype providertype);

    long countByStatus(com.example.treksathi.enums.AccountStatus status);

    long countByRoleAndCreatedAtBetween(com.example.treksathi.enums.Role role, java.time.LocalDateTime start,
            java.time.LocalDateTime end);

    java.util.List<User> findTop5ByOrderByCreatedAtDesc();

    long countByCreatedAtAfter(java.time.LocalDateTime date);

    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
