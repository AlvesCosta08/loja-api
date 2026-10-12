package com.alvescosta.loja.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final Counter ordersCreated;
    private final Counter paymentsConfirmed;

    public MetricsService(MeterRegistry registry) {
        this.ordersCreated = Counter.builder("loja.orders.created")
                .description("Pedidos criados com sucesso")
                .register(registry);

        this.paymentsConfirmed = Counter.builder("loja.payments.confirmed")
                .description("Pagamentos confirmados")
                .register(registry);
    }

    public void orderCreated() {
        ordersCreated.increment();
    }

    public void paymentConfirmed() {
        paymentsConfirmed.increment();
    }
}
