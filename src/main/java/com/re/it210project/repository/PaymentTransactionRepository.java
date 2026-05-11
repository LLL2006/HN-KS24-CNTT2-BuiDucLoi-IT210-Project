package com.re.it210project.repository;

import com.re.it210project.model.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction>
    findByTransactionRef(String transactionRef);
}