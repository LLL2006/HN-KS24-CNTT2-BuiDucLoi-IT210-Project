package com.re.it210project.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionRef;

    private Double amount;

    private String paymentProvider;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    @OneToOne
    @JoinColumn(name = "borrowing_record_id")
    private BorrowingRecord borrowingRecord;
}