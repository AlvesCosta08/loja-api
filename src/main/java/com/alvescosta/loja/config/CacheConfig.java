package com.alvescosta.loja.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Habilita o suporte a cache do Spring. O gerenciador concreto
 * (Redis em produção, ConcurrentMap nos testes) é escolhido pelo
 * Spring Boot com base em `spring.cache.type`.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
