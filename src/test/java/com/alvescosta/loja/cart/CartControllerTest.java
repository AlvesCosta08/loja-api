package com.alvescosta.loja.cart;

import com.alvescosta.loja.domain.Product;
import com.alvescosta.loja.domain.Role;
import com.alvescosta.loja.domain.User;
import com.alvescosta.loja.dto.AddToCartRequest;
import com.alvescosta.loja.dto.UpdateCartItemRequest;
import com.alvescosta.loja.repository.CartItemRepository;
import com.alvescosta.loja.repository.ProductRepository;
import com.alvescosta.loja.repository.UserRepository;
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
@DisplayName("CartController — Carrinho de compras")
class CartControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private User user;
    private User otherUser;
    private Product product;
    private String token;
    private String otherToken;

    @BeforeEach
    void setup() {
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("user@test.com").name("User")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.CUSTOMER).build());

        otherUser = userRepository.save(User.builder()
                .email("other@test.com").name("Other")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.CUSTOMER).build());

        product = productRepository.save(Product.builder()
                .name("Notebook").price(new BigDecimal("2500.00"))
                .stock(10).category("Eletronicos").build());

        token = jwtService.generate(user);
        otherToken = jwtService.generate(otherUser);
    }

    // ---------- AUTENTICAÇÃO ----------

    @Test
    @DisplayName("401 ao acessar carrinho sem token")
    void naoDeveAcessarCarrinhoSemToken() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401 ao adicionar item sem token")
    void naoDeveAdicionarItemSemToken() throws Exception {
        AddToCartRequest req = new AddToCartRequest(product.getId(), 1);
        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ---------- ADICIONAR ITEM ----------

    @Test
    @DisplayName("201 ao adicionar item válido")
    void deveAdicionarItemAoCarrinho() throws Exception {
        AddToCartRequest req = new AddToCartRequest(product.getId(), 2);

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(2500.00))
                .andExpect(jsonPath("$.items[0].subtotal").value(5000.00))
                .andExpect(jsonPath("$.total").value(5000.00))
                .andExpect(jsonPath("$.itemCount").value(2));
    }

    @Test
    @DisplayName("400 ao adicionar quantidade zero/negativa")
    void naoDeveAdicionarQuantidadeZeroOuNegativa() throws Exception {
        AddToCartRequest req = new AddToCartRequest(product.getId(), 0);

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("404 ao adicionar produto inexistente")
    void naoDeveAdicionarProdutoInexistente() throws Exception {
        AddToCartRequest req = new AddToCartRequest(9999L, 1);

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 ao adicionar acima do estoque")
    void naoDeveAdicionarAlemDoEstoque() throws Exception {
        AddToCartRequest req = new AddToCartRequest(product.getId(), 11);

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Soma quantidade quando produto já está no carrinho")
    void deveSomarQuantidadeQuandoProdutoJaEstaNoCarrinho() throws Exception {
        mockMvc.perform(post("/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new AddToCartRequest(product.getId(), 2))));

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddToCartRequest(product.getId(), 3))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.total").value(12500.00));
    }

    // ---------- LISTAR CARRINHO ----------

    @Test
    @DisplayName("200 com lista vazia e total zero")
    void deveListarCarrinhoVazio() throws Exception {
        mockMvc.perform(get("/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.itemCount").value(0));
    }

    @Test
    @DisplayName("Total calculado com múltiplos itens")
    void deveRetornarTotalCalculadoCorretamente() throws Exception {
        Product p2 = productRepository.save(Product.builder()
                .name("Mouse").price(new BigDecimal("150.00"))
                .stock(20).category("Eletronicos").build());

        mockMvc.perform(post("/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new AddToCartRequest(product.getId(), 1))));

        mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddToCartRequest(p2.getId(), 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.total").value(2800.00))
                .andExpect(jsonPath("$.itemCount").value(3));
    }

    @Test
    @DisplayName("Cada usuário vê apenas o próprio carrinho")
    void cadaUsuarioVeApenasSeuProprioCarrinho() throws Exception {
        mockMvc.perform(post("/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new AddToCartRequest(product.getId(), 2))));

        mockMvc.perform(get("/cart")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));
    }

    // ---------- ATUALIZAR QUANTIDADE ----------

    @Test
    @DisplayName("200 ao atualizar quantidade")
    void deveAtualizarQuantidadeDeUmItem() throws Exception {
        String body = mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddToCartRequest(product.getId(), 1))))
                .andReturn().getResponse().getContentAsString();

        Long itemId = objectMapper.readTree(body).get("items").get(0).get("id").asLong();

        mockMvc.perform(put("/cart/items/" + itemId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateCartItemRequest(5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.total").value(12500.00));
    }

    @Test
    @DisplayName("400 ao atualizar acima do estoque")
    void naoDeveAtualizarParaQuantidadeMaiorQueEstoque() throws Exception {
        String body = mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddToCartRequest(product.getId(), 1))))
                .andReturn().getResponse().getContentAsString();

        Long itemId = objectMapper.readTree(body).get("items").get(0).get("id").asLong();

        mockMvc.perform(put("/cart/items/" + itemId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateCartItemRequest(11))))
                .andExpect(status().isBadRequest());
    }

    // ---------- REMOVER ----------

    @Test
    @DisplayName("204 ao remover item")
    void deveRemoverItemDoCarrinho() throws Exception {
        String body = mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddToCartRequest(product.getId(), 1))))
                .andReturn().getResponse().getContentAsString();

        Long itemId = objectMapper.readTree(body).get("items").get(0).get("id").asLong();

        mockMvc.perform(delete("/cart/items/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    @DisplayName("404 ao tentar remover item de outro usuário")
    void naoDeveRemoverItemDeOutroUsuario() throws Exception {
        String body = mockMvc.perform(post("/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddToCartRequest(product.getId(), 1))))
                .andReturn().getResponse().getContentAsString();

        Long itemId = objectMapper.readTree(body).get("items").get(0).get("id").asLong();

        mockMvc.perform(delete("/cart/items/" + itemId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }
}