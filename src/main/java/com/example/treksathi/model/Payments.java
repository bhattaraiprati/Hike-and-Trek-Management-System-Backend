package com.example.treksathi.model;

import com.example.treksathi.enums.PaymentMethod;
import com.example.treksathi.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    private String paymentGatewayId;
    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime transactionDate;
    private String transactionReference;

}
