package com.example.treksathi.model;

import com.example.treksathi.enums.PaymentMethod;
import com.example.treksathi.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "registration_id", nullable = false)
    private EventRegistration eventRegistration;

    private String transactionUuid;
    private Double amount;
    private Double fee;
    private Double netAmount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @CreationTimestamp
    private LocalDateTime transactionDate;

    private String transactionReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;
    private LocalDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "released_by")
    private User releasedBy;
    private LocalDateTime releasedAt;

    // Keeping releasedDate for compatibility if needed, but releasedAt is the main
    // one
    private LocalDateTime releasedDate;

    @Column(columnDefinition = "TEXT")
    private String releaseNotes;
}
