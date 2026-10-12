package com.alvescosta.loja.admin;

import com.alvescosta.loja.domain.*;
import com.alvescosta.loja.dto.AddToCartRequest;
import com.alvescosta.loja.dto.RestockRequest;
import com.alvescosta.loja.dto.UpdateRoleRequest;
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
@DisplayName("AdminController — Painel administrativo")
class AdminControllerTest {

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
    private String customerToken;
    private String adminToken;

    @BeforeEach
    void setup() {
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
                .stock(10).category("Papelaria").build());

        customerToken = jwtService.generate(customer);
        adminToken = jwtService.generate(admin);
    }

    // ---------- AUTORIZAÇÃO ----------

    @Test
    @DisplayName("401 sem token em /admin/dashboard")
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("403 quando CUSTOMER tenta acessar dashboard")
    void clienteNaoPodeAcessarDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("403 quando CUSTOMER tenta listar usuários")
    void clienteNaoPodeListarUsuarios() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    // ---------- DASHBOARD ----------

    @Test
    @DisplayName("200 dashboard com totais zerados quando não há pedidos")
    void dashboardZerado() throws Exception {
        mockMvc.perform(get("/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(0))
                .andExpect(jsonPath("$.totalRevenue").value(0))
                .andExpect(jsonPath("$.averageTicket").value(0));
    }

    @Test
    @DisplayName("200 dashboard com totais corretos após pedidos")
    void dashboardComPedidos() throws Exception {
        criarPedidoPago(customer, product, 2); // 100.00

        Product p2 = productRepository.save(Product.builder()
                .name("Caderno").price(new BigDecimal("30.00"))
                .stock(20).category("Papelaria").build());
        criarPedidoPago(customer, p2, 1); // 30.00

        mockMvc.perform(get("/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(2))
                .andExpect(jsonPath("$.totalRevenue").value(130.00))
                .andExpect(jsonPath("$.averageTicket").value(65.00));
    }

    // ---------- LISTAGEM DE PEDIDOS ----------

    @Test
    @DisplayName("200 ADMIN lista todos os pedidos")
    void adminListaTodosOsPedidos() throws Exception {
        criarPedidoPago(customer, product, 1);
        criarPedidoPago(admin, product, 1);

        mockMvc.perform(get("/admin/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    // ---------- LISTAGEM DE USUÁRIOS ----------

    @Test
    @DisplayName("200 ADMIN lista clientes com total gasto")
    void adminListaUsuariosComTotalGasto() throws Exception {
        criarPedidoPago(customer, product, 3); // 150.00

        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // admin + customer
                .andExpect(jsonPath("$[?(@.email=='cli@test.com')].totalSpent")
                        .value(hasItem(150.00)))
                .andExpect(jsonPath("$[?(@.email=='cli@test.com')].orderCount")
                        .value(hasItem(1)));
    }

    // ---------- MUDANÇA DE ROLE ----------

    @Test
    @DisplayName("200 ADMIN promove CUSTOMER para ADMIN")
    void adminDevePromoverUsuario() throws Exception {
        UpdateRoleRequest req = new UpdateRoleRequest(Role.ADMIN);

        mockMvc.perform(patch("/admin/users/" + customer.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        User atualizado = userRepository.findById(customer.getId()).orElseThrow();
        assert atualizado.getRole() == Role.ADMIN;
    }

    @Test
    @DisplayName("400 ADMIN não pode rebaixar a si mesmo")
    void adminNaoPodeRebaixarASiMesmo() throws Exception {
        UpdateRoleRequest req = new UpdateRoleRequest(Role.CUSTOMER);

        mockMvc.perform(patch("/admin/users/" + admin.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---------- ESTOQUE BAIXO ----------

    @Test
    @DisplayName("200 lista produtos com estoque baixo")
    void deveListarProdutosComEstoqueBaixo() throws Exception {
        Product estoqueBaixo = productRepository.save(Product.builder()
                .name("Borracha").price(new BigDecimal("5.00"))
                .stock(2).category("Papelaria").build());

        mockMvc.perform(get("/admin/products/low-stock")
                        .param("threshold", "5")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(estoqueBaixo.getId()))
                .andExpect(jsonPath("$[0].stock").value(2));
    }

    // ---------- REPOSIÇÃO ----------

    @Test
    @DisplayName("200 ADMIN repõe estoque")
    void adminDeveReporEstoque() throws Exception {
        RestockRequest req = new RestockRequest(50);

        mockMvc.perform(post("/admin/products/" + product.getId() + "/restock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.stock").value(60));

        Product atualizado = productRepository.findById(product.getId()).orElseThrow();
        assert atualizado.getStock() == 60;
    }

    @Test
    @DisplayName("404 ao repor estoque de produto inexistente")
    void deveRetornar404AoReporProdutoInexistente() throws Exception {
        RestockRequest req = new RestockRequest(10);

        mockMvc.perform(post("/admin/products/9999/restock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ---------- helpers ----------

    private Long criarPedidoPago(User user, Product p, int qty) throws Exception {
        String token = jwtService.generate(user);

        mockMvc.perform(post("/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new AddToCartRequest(p.getId(), qty))));

        String body = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(body).get("id").asLong();

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return orderId;
    }
}