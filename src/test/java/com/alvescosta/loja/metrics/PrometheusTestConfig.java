package com.alvescosta.loja.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@AutoConfigureAfter({MetricsAutoConfiguration.class, PrometheusMetricsExportAutoConfiguration.class})
public class PrometheusTestConfig {

    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry(MeterRegistry registry) {
        // Se o auto-config já criou, não faz nada.
        if (registry instanceof PrometheusMeterRegistry p) {
            return p;
        }
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
}
