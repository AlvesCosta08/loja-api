package com.alvescosta.loja.config;

import com.alvescosta.loja.security.JwtAuthFilter;
import com.alvescosta.loja.security.RestAuthenticationEntryPoint;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String DEFAULT_JWT_SECRET =
            "change-me-in-production-with-at-least-32-chars";

    private final JwtAuthFilter jwtAuthFilter;
    private final RestAuthenticationEntryPoint entryPoint;

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Falha o boot se o JWT_SECRET default estiver sendo usado em produção.
     * Detecta produção por: env RENDER=true (Render injeta) OU
     * SPRING_PROFILES_ACTIVE=prod/production.
     */
    @PostConstruct
    void validateJwtSecret() {
        if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            String profile = System.getenv("SPRING_PROFILES_ACTIVE");
            boolean isProd = "true".equalsIgnoreCase(System.getenv("RENDER"))
                    || "prod".equalsIgnoreCase(profile)
                    || "production".equalsIgnoreCase(profile);
            if (isProd) {
                throw new IllegalStateException(
                        "JWT_SECRET não configurado em produção. " +
                        "Defina a env var JWT_SECRET com pelo menos 32 caracteres " +
                        "(ex: openssl rand -base64 48).");
            }
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/health").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/coupons/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/shipping/quote").permitAll()
                        .requestMatchers(HttpMethod.POST, "/webhooks/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}