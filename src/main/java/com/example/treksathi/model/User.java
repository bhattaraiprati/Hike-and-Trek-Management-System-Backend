package com.example.treksathi.model;

import com.example.treksathi.enums.AccountStatus;
import com.example.treksathi.enums.AuthProvidertype;
import com.example.treksathi.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String email;
    private String password;
    private String name;
    private String phone;
    private String profileImage;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Organizer organizer;

    private String providerId;
    @Enumerated(EnumType.STRING)
    private AuthProvidertype providerType = AuthProvidertype.LOCAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)

    private Role role = Role.HIKER;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.INACTIVE;

    @OneToMany(mappedBy = "user" ,cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EventRegistration> eventRegistration;

}
