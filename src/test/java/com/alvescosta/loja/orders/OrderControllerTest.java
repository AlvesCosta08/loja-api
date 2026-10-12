package com.alvescosta.loja.orders;

import com.alvescosta.loja.domain.OrderStatus;
import com.alvescosta.loja.domain.Product;
import com.alvescosta.loja.domain.Role;
import com.alvescosta.loja.domain.User;
import com.alvescosta.loja.dto.AddToCartRequest;
import com.alvescosta.loja.dto.UpdateOrderStatusRequest;
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
@DisplayName("OrderController — Checkout e Pedidos")
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private User customer;
    private User admin;
    private Product product;
    private String token;
    private String adminToken;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        customer = userRepository.save(User.builder()
                .email("cliente@test.com").name("Cliente")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.CUSTOMER).build());

        admin = userRepository.save(User.builder()
                .email("admin@test.com").name("Admin")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.ADMIN).build());

        product = productRepository.save(Product.builder()
                .name("Notebook").price(new BigDecimal("2500.00"))
                .stock(10).category("Eletronicos").build());

        token = jwtService.generate(customer);
        adminToken = jwtService.generate(admin);
    }

    @Test
    @DisplayName("401 ao criar pedido sem token")
    void naoDeveCriarPedidoSemToken() throws Exception {
        mockMvc.perform(post("/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("201 ao criar pedido a partir do carrinho")
    void deveCriarPedidoAPartirDoCarrinho() throws Exception {
        adicionarAoCarrinho(product.getId(), 2);

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName").value("Notebook"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(2500.00))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.total").value(5000.00));
    }

    @Test
    @DisplayName("400 ao criar pedido com carrinho vazio")
    void naoDeveCriarPedidoComCarrinhoVazio() throws Exception {
        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 ao tentar comprar mais que o estoque")
    void naoDeveCriarPedidoAcimaDoEstoque() throws Exception {
        Product pequeno = productRepository.save(Product.builder()
                .name("Item raro").price(new BigDecimal("100.00"))
                .stock(3).category("Geral").build());

        adicionarAoCarrinho(pequeno.getId(), 3);

        pequeno.setStock(1);
        productRepository.save(pequeno);

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Carrinho é limpo após criar pedido")
    void carrinhoDeveSerLimpoAposCheckout() throws Exception {
        adicionarAoCarrinho(product.getId(), 1);

        mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + token));

        mockMvc.perform(get("/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    @DisplayName("Estoque é decrementado após criar pedido")
    void estoqueDeveSerDecrementado() throws Exception {
        adicionarAoCarrinho(product.getId(), 3);

        mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        Product atualizado = productRepository.findById(product.getId()).orElseThrow();
        assert atualizado.getStock() == 7;
    }

    @Test
    @DisplayName("Pedido mantém snapshot do preço mesmo após produto mudar")
    void deveManterSnapshotDePrecos() throws Exception {
        adicionarAoCarrinho(product.getId(), 2);

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        product.setPrice(new BigDecimal("9999.00"));
        productRepository.save(product);

        mockMvc.perform(get("/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.content[0].items[0].unitPrice").value(2500.00))
                .andExpect(jsonPath("$.content[0].total").value(5000.00));
    }

    @Test
    @DisplayName("200 listando apenas pedidos do usuário logado")
    void usuarioVeApenasSeusPedidos() throws Exception {
        adicionarAoCarrinho(product.getId(), 1);
        mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + token));

        adicionarAoCarrinho(adminToken, product.getId(), 1);
        mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer " + adminToken));

        mockMvc.perform(get("/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("404 quando pedido não existe")
    void deveRetornar404QuandoPedidoNaoExiste() throws Exception {
        mockMvc.perform(get("/orders/9999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("403 quando outro CUSTOMER tenta ver pedido alheio")
    void naoDeveAcessarPedidoDeOutroUsuario() throws Exception {
        adicionarAoCarrinho(product.getId(), 1);
        String body = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(body).get("id").asLong();

        User outro = userRepository.save(User.builder()
                .email("outro@test.com").name("Outro")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.CUSTOMER).build());
        String outroToken = jwtService.generate(outro);

        mockMvc.perform(get("/orders/" + orderId)
                        .header("Authorization", "Bearer " + outroToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("200 dono vê seu pedido")
    void donoVeSeuPedido() throws Exception {
        adicionarAoCarrinho(product.getId(), 1);
        String body = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/orders/" + orderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("200 ADMIN muda status PENDING → PAID")
    void adminDeveMudarStatus() throws Exception {
        adicionarAoCarrinho(product.getId(), 1);
        String body = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(body).get("id").asLong();

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.PAID);

        mockMvc.perform(patch("/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @DisplayName("403 quando CUSTOMER tenta mudar status")
    void clienteNaoPodeMudarStatus() throws Exception {
        adicionarAoCarrinho(product.getId(), 1);
        String body = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(body).get("id").asLong();

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.PAID);

        mockMvc.perform(patch("/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ---------- FRETE NO CHECKOUT ----------

    @Test
    @DisplayName("Frete STANDARD é grátis quando subtotal >= R$ 200")
    void deveAplicarFreteGratisAcimaDe200() throws Exception {
        adicionarAoCarrinho(product.getId(), 1); // 2500.00

        mockMvc.perform(post("/orders").param("cep", "01310100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingCost").value(0))
                .andExpect(jsonPath("$.total").value(2500.00));
    }

    @Test
    @DisplayName("Cobra frete STANDARD quando subtotal < R$ 200")
    void deveCobrarFreteParaSubtotalBaixo() throws Exception {
        Product barato = productRepository.save(Product.builder()
                .name("Caneta").price(new BigDecimal("50.00"))
                .stock(10).category("Papelaria").build());

        adicionarAoCarrinho(barato.getId(), 1); // 50.00

        mockMvc.perform(post("/orders").param("cep", "01310100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingCost").value(10.00))
                .andExpect(jsonPath("$.total").value(60.00));
    }

    @Test
    @DisplayName("Não aplica frete quando CEP é omitido")
    void naoDeveAplicarFreteSemCep() throws Exception {
        adicionarAoCarrinho(product.getId(), 1); // 2500.00

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingCost").value(0))
                .andExpect(jsonPath("$.total").value(2500.00));
    }

    @Test
    @DisplayName("400 quando CEP inválido no checkout")
    void deveRejeitarCepInvalidoNoCheckout() throws Exception {
        adicionarAoCarrinho(product.getId(), 1);

        mockMvc.perform(post("/orders").param("cep", "123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ---------- helpers ----------

    private void adicionarAoCarrinho(Long productId, int qty) throws Exception {
        adicionarAoCarrinho(token, productId, qty);
    }

    private void adicionarAoCarrinho(String tok, Long productId, int qty) throws Exception {
        mockMvc.perform(post("/cart/items")
                .header("Authorization", "Bearer " + tok)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new AddToCartRequest(productId, qty))));
    }
}
