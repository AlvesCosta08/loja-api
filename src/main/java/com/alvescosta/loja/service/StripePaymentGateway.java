package com.alvescosta.loja.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementação mock do Stripe. Em produção, isso chamaria a API real
 * (PaymentIntent) e retornaria o client_secret verdadeiro.
 *
 * O gatewayRef e o clientSecret são determinísticos por pedido, o que
 * simplifica testes e conciliação.
 */
@Service
public class StripePaymentGateway implements PaymentGateway {

    @Override
    public Intent createIntent(Long orderId, BigDecimal amount) {
        String gatewayRef = "pi_mock_" + orderId + "_" + UUID.nameUUIDFromBytes(
                (orderId + ":" + amount.toPlainString()).getBytes()).toString().substring(0, 8);

        String clientSecret = gatewayRef + "_secret_" + UUID.randomUUID().toString().substring(0, 12);

        return new Intent(gatewayRef, clientSecret);
    }
}
