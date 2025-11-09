package com.example.treksathi.model;

import com.example.treksathi.enums.AuthProvidertype;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;
    private String password;
    private String name;

    private String providerId;
    @Enumerated(EnumType.STRING)
    private AuthProvidertype providerType;

    private String role;

}
