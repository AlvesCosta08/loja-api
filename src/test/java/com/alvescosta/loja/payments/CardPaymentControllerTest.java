package com.alvescosta.loja.payments;

import com.alvescosta.loja.domain.*;
import com.alvescosta.loja.dto.AddToCartRequest;
import com.alvescosta.loja.repository.*;
import com.alvescosta.loja.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CardPaymentController — Pagamento cartão/Stripe")
class CardPaymentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private User customer;
    private User admin;
    private Product product;
    private String token;
    private String adminToken;

    @BeforeEach
    void setup() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        customer = userRepository.save(User.builder()
                .email("cli@test.com").name("Cli")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.CUSTOMER).build());

        admin = userRepository.save(User.builder()
                .email("adm@test.com").name("Adm")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.ADMIN).build());

        product = productRepository.save(Product.builder()
                .name("Caneta").price(new BigDecimal("50.00"))
                .stock(20).category("Papelaria").build());

        token = jwtService.generate(customer);
        adminToken = jwtService.generate(admin);
    }

    // ---------- CRIAR PAYMENT INTENT ----------

    @Test
    @DisplayName("201 ao criar PaymentIntent para pedido PENDING")
    void deveCriarPaymentIntent() throws Exception {
        Long orderId = criarPedido();

        mockMvc.perform(post("/payments/card")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.method").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.clientSecret").isNotEmpty())
                .andExpect(jsonPath("$.gatewayRef").isNotEmpty());
    }

    @Test
    @DisplayName("401 sem token")
    void naoDeveCriarSemToken() throws Exception {
        mockMvc.perform(post("/payments/card").param("orderId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("404 pedido inexistente")
    void deveRetornar404() throws Exception {
        mockMvc.perform(post("/payments/card")
                        .param("orderId", "9999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("403 outro CUSTOMER")
    void naoDevePagarPedidoDeOutroUsuario() throws Exception {
        Long orderId = criarPedido();

        User outro = userRepository.save(User.builder()
                .email("outro@test.com").name("Outro")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.CUSTOMER).build());
        String outroToken = jwtService.generate(outro);

        mockMvc.perform(post("/payments/card")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + outroToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("400 pedido não está PENDING")
    void naoDevePagarPedidoJaPago() throws Exception {
        Long orderId = criarPedido();
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        mockMvc.perform(post("/payments/card")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ---------- WEBHOOK ----------

    @Test
    @DisplayName("200 webhook confirma pagamento e pedido vira PAID")
    void webhookDeveConfirmarPagamento() throws Exception {
        Long orderId = criarPedido();

        String body = mockMvc.perform(post("/payments/card")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        String gatewayRef = objectMapper.readTree(body).get("gatewayRef").asText();

        mockMvc.perform(post("/webhooks/stripe")
                        .header("Stripe-Signature", "test-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "type", "payment_intent.succeeded",
                                "data", Map.of("object", Map.of("id", gatewayRef))
                        ))))
                .andExpect(status().isOk());

        Order atualizado = orderRepository.findById(orderId).orElseThrow();
        assert atualizado.getStatus() == OrderStatus.PAID;
    }

    @Test
    @DisplayName("400 webhook sem assinatura")
    void webhookSemAssinaturaDeveFalhar() throws Exception {
        mockMvc.perform(post("/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Idempotência: webhook duplicado não re-paga")
    void webhookDuplicadoNaoDeveRepagar() throws Exception {
        Long orderId = criarPedido();

        String body = mockMvc.perform(post("/payments/card")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        String gatewayRef = objectMapper.readTree(body).get("gatewayRef").asText();

        String payload = objectMapper.writeValueAsString(Map.of(
                "type", "payment_intent.succeeded",
                "data", Map.of("object", Map.of("id", gatewayRef))
        ));

        mockMvc.perform(post("/webhooks/stripe")
                        .header("Stripe-Signature", "test-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        mockMvc.perform(post("/webhooks/stripe")
                        .header("Stripe-Signature", "test-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        assert paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.CONFIRMED)
                .count() == 1;
    }

    // ---------- BUSCAR ----------

    @Test
    @DisplayName("200 dono busca PaymentIntent")
    void donoBuscaPaymentIntent() throws Exception {
        Long orderId = criarPedido();

        String body = mockMvc.perform(post("/payments/card")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long paymentId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/payments/" + paymentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method").value("CREDIT_CARD"));
    }

    // ---------- helpers ----------

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
