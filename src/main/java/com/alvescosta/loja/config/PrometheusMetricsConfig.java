package com.alvescosta.loja.config;

import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra explicitamente o PrometheusMeterRegistry para garantir que o
 * endpoint /actuator/prometheus seja exposto. O auto-config do Spring Boot
 * 3.4 + Micrometer 1.14 pode pular a criação quando há injeção precoce do
 * MeterRegistry em serviços de negócio (MetricsService).
 */
@Configuration
public class PrometheusMetricsConfig {

    @Bean
    @ConditionalOnMissingBean(PrometheusMeterRegistry.class)
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
}
