package com.alvescosta.loja.metrics;

import com.alvescosta.loja.domain.*;
import com.alvescosta.loja.dto.AddToCartRequest;
import com.alvescosta.loja.repository.*;
import com.alvescosta.loja.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Métricas — Actuator + Micrometer")
class MetricsTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @Autowired MeterRegistry meterRegistry;
    @Autowired CacheManager cacheManager;

    private User customer;
    private User admin;
    private Product product;
    private String token;
    private String adminToken;

    @BeforeEach
    void setup() {
        cacheManager.getCacheNames()
                .forEach(name -> cacheManager.getCache(name).clear());

        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        customer = userRepository.save(User.builder()
                .email("cli@test.com").name("Cliente")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.CUSTOMER).build());

        admin = userRepository.save(User.builder()
                .email("adm@test.com").name("Admin")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.ADMIN).build());

        product = productRepository.save(Product.builder()
                .name("Caneta").price(new BigDecimal("50.00"))
                .stock(20).category("Papelaria").build());

        token = jwtService.generate(customer);
        adminToken = jwtService.generate(admin);
    }

    // ---------- ACTUATOR ----------

    @Test
    @DisplayName("200 /actuator/health sem autenticação")
    void healthPublico() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("200 /actuator/info sem autenticação")
    void infoPublico() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("401 /actuator/metrics sem autenticação")
    void metricsExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("200 /actuator/metrics com token ADMIN")
    void metricsComAdmin() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ---------- MÉTRICAS CUSTOMIZADAS ----------

    @Test
    @DisplayName("Contador loja.orders.created incrementa após checkout")
    void contadorPedidosIncrementa() throws Exception {
        double antes = valorContador("loja.orders.created");

        criarPedido();

        double depois = valorContador("loja.orders.created");

        assert depois == antes + 1;
    }

    @Test
    @DisplayName("Contador loja.payments.confirmed incrementa após confirmação")
    void contadorPagamentosIncrementa() throws Exception {
        Long orderId = criarPedido();

        String body = mockMvc.perform(post("/payments")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long paymentId = objectMapper.readTree(body).get("id").asLong();

        double antes = valorContador("loja.payments.confirmed");

        mockMvc.perform(post("/payments/" + paymentId + "/confirm")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        double depois = valorContador("loja.payments.confirmed");

        assert depois == antes + 1;
    }

    // ---------- PROMETHEUS ----------

    @Test
    @DisplayName("200 /actuator/metrics/loja.orders.created expõe métrica customizada")
    void metricaPedidosExposta() throws Exception {
        criarPedido();

        mockMvc.perform(get("/actuator/metrics/loja.orders.created")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("loja.orders.created"))
                .andExpect(jsonPath("$.measurements[0].value",
                        greaterThanOrEqualTo(1.0)));
    }

    // ---------- helpers ----------

    private double valorContador(String nome) {
        var counter = meterRegistry.find(nome).counter();
        return counter != null ? counter.count() : 0.0;
    }

    private Long criarPedido() throws Exception {
        mockMvc.perform(post("/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new AddToCartRequest(product.getId(), 1))));

        String body = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }
}