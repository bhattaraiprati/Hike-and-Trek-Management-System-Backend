package com.example.treksathi.repository;

import com.example.treksathi.enums.AuthProvidertype;
import com.example.treksathi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByProviderIdAndProviderType(String providerId, AuthProvidertype providertype);
}
