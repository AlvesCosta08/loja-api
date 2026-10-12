package com.alvescosta.loja.products;

import com.alvescosta.loja.domain.Product;
import com.alvescosta.loja.domain.Role;
import com.alvescosta.loja.domain.User;
import com.alvescosta.loja.dto.ProductRequest;
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
@DisplayName("ProductController — CRUD de produtos")
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @Autowired org.springframework.cache.CacheManager cacheManager;

    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setup() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        productRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(User.builder()
                .email("admin@test.com")
                .name("Admin")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.ADMIN)
                .build());

        User customer = userRepository.save(User.builder()
                .email("cliente@test.com")
                .name("Cliente")
                .hashedPassword(passwordEncoder.encode("senha12345"))
                .role(Role.CUSTOMER)
                .build());

        adminToken = jwtService.generate(admin);
        customerToken = jwtService.generate(customer);
    }

    // ---------- CRIAÇÃO ----------

    @Test
    @DisplayName("201 quando ADMIN cria produto")
    void adminDeveCriarProduto() throws Exception {
        ProductRequest req = new ProductRequest(
                "Notebook", new BigDecimal("2500.00"), 10, "Eletronicos", "img.png");

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Notebook"))
                .andExpect(jsonPath("$.price").value(2500.00))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    @DisplayName("403 quando CUSTOMER tenta criar produto")
    void clienteNaoPodeCriarProduto() throws Exception {
        ProductRequest req = new ProductRequest(
                "Notebook", new BigDecimal("2500.00"), 10, "Eletronicos", null);

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("400 quando preço é negativo")
    void naoDeveCriarProdutoComPrecoNegativo() throws Exception {
        ProductRequest req = new ProductRequest(
                "Notebook", new BigDecimal("-1.00"), 10, "Eletronicos", null);

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 quando nome está em branco")
    void naoDeveCriarProdutoSemNome() throws Exception {
        ProductRequest req = new ProductRequest(
                "", new BigDecimal("2500.00"), 10, "Eletronicos", null);

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---------- LISTAGEM ----------

    @Test
    @DisplayName("200 quando qualquer pessoa lista produtos")
    void qualquerPessoaPodeListarProdutos() throws Exception {
        criarProduto("Notebook", "2500.00", "Eletronicos");

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("200 com paginação padrão (size=10)")
    void deveListarComPaginacao() throws Exception {
        for (int i = 1; i <= 15; i++) {
            criarProduto("Produto " + i, "10.00", "Geral");
        }

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements").value(15));
    }

    @Test
    @DisplayName("200 filtrando por categoria")
    void deveFiltrarPorCategoria() throws Exception {
        criarProduto("Notebook", "2500.00", "Eletronicos");
        criarProduto("Camisa", "80.00", "Vestuario");

        mockMvc.perform(get("/products").param("category", "Eletronicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Notebook"));
    }

    @Test
    @DisplayName("200 buscando por nome (case-insensitive)")
    void deveBuscarPorNome() throws Exception {
        criarProduto("Notebook Dell", "2500.00", "Eletronicos");
        criarProduto("Mouse Logitech", "150.00", "Eletronicos");

        mockMvc.perform(get("/products").param("search", "note"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Notebook Dell"));
    }

    // ---------- BUSCA POR ID ----------

    @Test
    @DisplayName("404 quando produto não existe")
    void deveRetornar404QuandoProdutoNaoExiste() throws Exception {
        mockMvc.perform(get("/products/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("200 ao buscar produto por ID")
    void deveBuscarProdutoPorId() throws Exception {
        Long id = criarProduto("Notebook", "2500.00", "Eletronicos");

        mockMvc.perform(get("/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Notebook"));
    }

    // ---------- ATUALIZAÇÃO ----------

    @Test
    @DisplayName("200 quando ADMIN atualiza produto")
    void adminDeveAtualizarProduto() throws Exception {
        Long id = criarProduto("Notebook", "2500.00", "Eletronicos");

        ProductRequest req = new ProductRequest(
                "Notebook Pro", new BigDecimal("3000.00"), 5, "Eletronicos", null);

        mockMvc.perform(put("/products/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook Pro"))
                .andExpect(jsonPath("$.price").value(3000.00));
    }

    @Test
    @DisplayName("403 quando CUSTOMER tenta atualizar produto")
    void clienteNaoPodeAtualizarProduto() throws Exception {
        Long id = criarProduto("Notebook", "2500.00", "Eletronicos");

        ProductRequest req = new ProductRequest(
                "Hackeado", new BigDecimal("1.00"), 999, "Eletronicos", null);

        mockMvc.perform(put("/products/" + id)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ---------- DELEÇÃO ----------

    @Test
    @DisplayName("204 quando ADMIN deleta produto")
    void adminDeveDeletarProduto() throws Exception {
        Long id = criarProduto("Notebook", "2500.00", "Eletronicos");

        mockMvc.perform(delete("/products/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("404 após deletar produto")
    void deveRetornar404AposDeletar() throws Exception {
        Long id = criarProduto("Notebook", "2500.00", "Eletronicos");

        mockMvc.perform(delete("/products/" + id)
                .header("Authorization", "Bearer " + adminToken));

        mockMvc.perform(get("/products/" + id))
                .andExpect(status().isNotFound());
    }

    // ---------- helpers ----------

    private Long criarProduto(String name, String price, String category) {
        Product p = productRepository.save(Product.builder()
                .name(name)
                .price(new BigDecimal(price))
                .stock(10)
                .category(category)
                .build());
        return p.getId();
    }
}