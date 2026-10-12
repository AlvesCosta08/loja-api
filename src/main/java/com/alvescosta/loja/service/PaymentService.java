package com.alvescosta.loja.service;

import com.alvescosta.loja.domain.*;
import com.alvescosta.loja.dto.PaymentResponse;
import com.alvescosta.loja.exception.BusinessException;
import com.alvescosta.loja.exception.NotFoundException;
import com.alvescosta.loja.repository.OrderRepository;
import com.alvescosta.loja.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final int PIX_EXPIRATION_MINUTES = 30;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    // -------------------------------------------------------------
    // PIX
    // -------------------------------------------------------------

    @Transactional
    public PaymentResponse createPix(Long orderId, Long userId, boolean isAdmin) {
        Order order = loadOrderForPayment(orderId, userId, isAdmin);

        PixGenerator.Pix pix = PixGenerator.generate(
                order.getId(),
                order.getTotal().toPlainString());

        Payment payment = Payment.builder()
                .order(order)
                .method(PaymentMethod.PIX)
                .status(PaymentStatus.PENDING)
                .amount(order.getTotal())
                .pixCopyPaste(pix.copyPaste())
                .pixQrCodeBase64(pix.qrCodeBase64())
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(PIX_EXPIRATION_MINUTES, ChronoUnit.MINUTES))
                .build();

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // -------------------------------------------------------------
    // Cartão / Stripe
    // -------------------------------------------------------------

    @Transactional
    public PaymentResponse createCard(Long orderId, Long userId, boolean isAdmin) {
        Order order = loadOrderForPayment(orderId, userId, isAdmin);

        PaymentGateway.Intent intent = paymentGateway.createIntent(
                order.getId(), order.getTotal());

        Payment payment = Payment.builder()
                .order(order)
                .method(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.PENDING)
                .amount(order.getTotal())
                .gatewayRef(intent.gatewayRef())
                .clientSecret(intent.clientSecret())
                .createdAt(Instant.now())
                .build();

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    /**
     * Processa o webhook do gateway. É idempotente: se o pagamento já está
     * CONFIRMED, retorna 200 sem re-processar.
     */
    @Transactional
    public PaymentResponse handleGatewayWebhook(String gatewayRef) {
        if (gatewayRef == null || gatewayRef.isBlank()) {
            throw new BusinessException("gatewayRef obrigatorio");
        }

        Payment payment = paymentRepository.findByGatewayRef(gatewayRef)
                .orElseThrow(() -> new NotFoundException("Pagamento nao encontrado"));

        // Idempotência: se já confirmado, não faz nada
        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            return PaymentResponse.from(payment);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(
                    "Pagamento nao esta PENDING (atual: " + payment.getStatus() + ")");
        }

        payment.setStatus(PaymentStatus.CONFIRMED);
        payment.setConfirmedAt(Instant.now());
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return PaymentResponse.from(payment);
    }

    // -------------------------------------------------------------
    // Consulta / confirmação manual (PIX)
    // -------------------------------------------------------------

    @Transactional
    public PaymentResponse confirm(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Pagamento nao encontrado"));

        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            throw new BusinessException("Pagamento ja confirmado");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Pagamento nao esta PENDING");
        }

        payment.setStatus(PaymentStatus.CONFIRMED);
        payment.setConfirmedAt(Instant.now());
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse findByIdForUser(Long paymentId, Long userId, boolean isAdmin) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Pagamento nao encontrado"));

        if (!isAdmin && !payment.getOrder().getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Acesso negado");
        }
        return PaymentResponse.from(payment);
    }

    // -------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------

    private Order loadOrderForPayment(Long orderId, Long userId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Pedido nao encontrado"));

        if (!isAdmin && !order.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Acesso negado");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Pedido nao esta em PENDING (atual: " + order.getStatus() + ")");
        }

        return order;
    }
}
