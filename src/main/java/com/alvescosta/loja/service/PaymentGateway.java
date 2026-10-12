package com.alvescosta.loja.service;

import java.math.BigDecimal;

/**
 * Abstração para gateways de pagamento. Permite trocar PIX / Stripe / outros
 * sem alterar o PaymentService.
 */
public interface PaymentGateway {

    /**
     * Cria uma intenção de pagamento no gateway.
     *
     * @param orderId  identificador do pedido na loja (usado como idempotency key)
     * @param amount   valor total
     * @return dados para o frontend completar o pagamento
     */
    Intent createIntent(Long orderId, BigDecimal amount);

    /**
     * Dados retornados pelo gateway para o frontend.
     *
     * @param gatewayRef    ID do recurso no gateway (usado para conciliação e webhook)
     * @param clientSecret  token que o frontend usa para finalizar o pagamento
     */
    record Intent(String gatewayRef, String clientSecret) {}
}
