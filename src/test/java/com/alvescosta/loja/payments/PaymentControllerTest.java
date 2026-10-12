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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PaymentController — Pagamento Pix")
class PaymentControllerTest {

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

    // ---------- AUTENTICAÇÃO ----------

    @Test
    @DisplayName("401 ao criar pagamento sem token")
    void naoDeveCriarPagamentoSemToken() throws Exception {
        mockMvc.perform(post("/payments").param("orderId", "1"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- CRIAR PAGAMENTO ----------

    @Test
    @DisplayName("201 ao gerar Pix para pedido PENDING do próprio usuário")
    void deveCriarPagamentoPix() throws Exception {
        Long orderId = criarPedido();

        mockMvc.perform(post("/payments")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.method").value("PIX"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.pixCopyPaste").isNotEmpty())
                .andExpect(jsonPath("$.pixQrCodeBase64").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    @DisplayName("404 quando pedido não existe")
    void deveRetornar404QuandoPedidoNaoExiste() throws Exception {
        mockMvc.perform(post("/payments")
                        .param("orderId", "9999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("403 quando outro CUSTOMER tenta pagar pedido alheio")
    void naoDevePagarPedidoDeOutroUsuario() throws Exception {
        Long orderId = criarPedido();

        User outro = userRepository.save(User.builder()
                .email("outro@test.com").name("Outro")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.CUSTOMER).build());
        String outroToken = jwtService.generate(outro);

        mockMvc.perform(post("/payments")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + outroToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("400 ao tentar pagar pedido já PAID")
    void naoDevePagarPedidoJaPago() throws Exception {
        Long orderId = criarPedido();
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        mockMvc.perform(post("/payments")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ---------- CONFIRMAR PAGAMENTO (ADMIN) ----------

    @Test
    @DisplayName("200 ADMIN confirma pagamento e pedido vira PAID")
    void adminDeveConfirmarPagamento() throws Exception {
        Long orderId = criarPedido();

        String body = mockMvc.perform(post("/payments")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long paymentId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(post("/payments/" + paymentId + "/confirm")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.confirmedAt").isNotEmpty());

        Order atualizado = orderRepository.findById(orderId).orElseThrow();
        assert atualizado.getStatus() == OrderStatus.PAID;
    }

    @Test
    @DisplayName("403 quando CUSTOMER tenta confirmar pagamento")
    void clienteNaoPodeConfirmarPagamento() throws Exception {
        Long orderId = criarPedido();

        String body = mockMvc.perform(post("/payments")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long paymentId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(post("/payments/" + paymentId + "/confirm")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ---------- BUSCAR ----------

    @Test
    @DisplayName("200 dono busca pagamento")
    void donoBuscaPagamento() throws Exception {
        Long orderId = criarPedido();

        String body = mockMvc.perform(post("/payments")
                        .param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long paymentId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/payments/" + paymentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.method").value("PIX"));
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