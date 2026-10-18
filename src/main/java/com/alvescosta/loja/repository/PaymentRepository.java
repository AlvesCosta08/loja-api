package com.alvescosta.loja.repository;

import com.alvescosta.loja.domain.Payment;
import com.alvescosta.loja.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findFirstByOrderIdAndStatus(Long orderId, PaymentStatus status);

    Optional<Payment> findByGatewayRef(String gatewayRef);

    Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);
}