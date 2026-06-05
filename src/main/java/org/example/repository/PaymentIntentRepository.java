package org.example.repository;

import org.example.domain.PaymentIntent;
import org.example.domain.enums.PaymentIntentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {

    Optional<PaymentIntent> findByOrder_Id(Long orderId);

    Optional<PaymentIntent> findByPaymentTxHash(String paymentTxHash);

    boolean existsByPaymentTxHash(String paymentTxHash);

    List<PaymentIntent> findByStatus(PaymentIntentStatus status);
}