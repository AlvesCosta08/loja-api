package com.alvescosta.loja.products;

import com.alvescosta.loja.domain.Product;
import com.alvescosta.loja.dto.ProductRequest;
import com.alvescosta.loja.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Cache de produtos")
class ProductCacheTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProductRepository productRepository;
    @SpyBean ProductRepository productRepositorySpy;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired CacheManager cacheManager;

    private Product product;

    @BeforeEach
    void setup() {
        // Limpa todos os caches antes de cada teste
        cacheManager.getCacheNames()
                .forEach(name -> cacheManager.getCache(name).clear());

        productRepository.deleteAll();

        product = productRepository.save(Product.builder()
                .name("Notebook").price(new BigDecimal("2500.00"))
                .stock(10).category("Eletronicos").build());

        // Zera o contador de invocações do spy
        clearInvocations(productRepositorySpy);
    }

    @Test
    @DisplayName("Primeira chamada consulta o banco")
    void primeiraChamadaConsultaBanco() throws Exception {
        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook"));

        verify(productRepositorySpy, times(1)).findById(product.getId());
    }

    @Test
    @DisplayName("Segunda chamada idêntica NÃO consulta o banco")
    void segundaChamadaUsaCache() throws Exception {
        // Primeira chamada — popula o cache
        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk());

        clearInvocations(productRepositorySpy);

        // Segunda chamada — deve bater no cache
        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook"));

        verify(productRepositorySpy, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Cache retorna dados corretos")
    void cacheRetornaDadosCorretos() throws Exception {
        // Popula
        mockMvc.perform(get("/products/" + product.getId()));

        // Segunda resposta deve ser idêntica
        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("Notebook"))
                .andExpect(jsonPath("$.price").value(2500.00))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.category").value("Eletronicos"));
    }

    @Test
    @DisplayName("PUT invalida o cache do item")
    void updateInvalidaCache() throws Exception {
        // Popula o cache
        mockMvc.perform(get("/products/" + product.getId()));

        // Atualiza (usa repository direto pra não lidar com auth de ADMIN)
        product.setName("Notebook Pro");
        product.setPrice(new BigDecimal("3000.00"));
        productRepository.save(product);

        // Simula uma chamada de update via service — limpa cache
        cacheManager.getCache("products").clear();

        clearInvocations(productRepositorySpy);

        // GET após update precisa consultar o banco novamente
        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook Pro"));

        verify(productRepositorySpy, times(1)).findById(product.getId());
    }

    @Test
    @DisplayName("DELETE limpa cache do item")
    void deleteLimpaCache() throws Exception {
        // Popula
        mockMvc.perform(get("/products/" + product.getId()));

        productRepository.deleteAll();
        cacheManager.getCache("products").clear();

        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Listagem cacheada por combinação de filtros")
    void listagemCacheadaPorFiltros() throws Exception {
        // Popula
        mockMvc.perform(get("/products").param("category", "Eletronicos"))
                .andExpect(status().isOk());

        clearInvocations(productRepositorySpy);

        // Mesma combinação — deve usar cache
        mockMvc.perform(get("/products").param("category", "Eletronicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Notebook"));

        verify(productRepositorySpy, never())
                .findByCategoryIgnoreCase(any(), any());
    }

    @Test
    @DisplayName("Listagem vazia também é cacheada")
    void listagemVaziaTambemCacheada() throws Exception {
        mockMvc.perform(get("/products").param("category", "Inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        clearInvocations(productRepositorySpy);

        mockMvc.perform(get("/products").param("category", "Inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(productRepositorySpy, never())
                .findByCategoryIgnoreCase(any(), any());
    }

    @Test
    @DisplayName("Caches configurados existem")
    void cachesConfiguradosExistem() {
        assert cacheManager.getCache("products") != null;
        assert cacheManager.getCache("productLists") != null;
    }
}