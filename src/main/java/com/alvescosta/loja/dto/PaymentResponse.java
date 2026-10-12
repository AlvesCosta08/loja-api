package com.alvescosta.loja.dto;

import com.alvescosta.loja.domain.Payment;
import com.alvescosta.loja.domain.PaymentMethod;
import com.alvescosta.loja.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long orderId,
        PaymentMethod method,
        PaymentStatus status,
        BigDecimal amount,
        String pixCopyPaste,
        String pixQrCodeBase64,
        Instant createdAt,
        Instant expiresAt,
        Instant confirmedAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrder().getId(),
                p.getMethod(),
                p.getStatus(),
                p.getAmount(),
                p.getPixCopyPaste(),
                p.getPixQrCodeBase64(),
                p.getCreatedAt(),
                p.getExpiresAt(),
                p.getConfirmedAt()
        );
    }
}
