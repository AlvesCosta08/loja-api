package com.alvescosta.loja.products;

import com.alvescosta.loja.domain.Product;
import com.alvescosta.loja.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Cache de produtos")
class ProductCacheTest {

    @Autowired MockMvc mockMvc;
    @Autowired CacheManager cacheManager;

    // Um único campo: o spy substitui o bean real do repositório
    @MockitoSpyBean ProductRepository productRepository;

    private Product product;

    @BeforeEach
    void setup() {
        // Limpa todos os caches antes de cada teste
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });

        productRepository.deleteAll();

        product = productRepository.save(Product.builder()
                .name("Notebook").price(new BigDecimal("2500.00"))
                .stock(10).category("Eletronicos").build());

        // Zera o contador de invocações do spy
        clearInvocations(productRepository);
    }

    @Test
    @DisplayName("Primeira chamada consulta o banco")
    void primeiraChamadaConsultaBanco() throws Exception {
        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook"));

        verify(productRepository, times(1)).findById(product.getId());
    }

    @Test
    @DisplayName("Segunda chamada idêntica NÃO consulta o banco")
    void segundaChamadaUsaCache() throws Exception {
        // Primeira chamada: popula o cache
        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk());

        clearInvocations(productRepository);

        // Segunda chamada: deve bater no cache
        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook"));

        verify(productRepository, never()).findById(anyLong());
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
    @DisplayName("Após limpar o cache, o GET consulta o banco de novo")
    void updateInvalidaCache() throws Exception {
        // ATENÇÃO: este teste NÃO valida o @CacheEvict do PUT; ele limpa o cache à mão.
        // Popula o cache
        mockMvc.perform(get("/products/" + product.getId()));

        // Atualiza direto no repositório (evita lidar com auth de ADMIN)
        product.setName("Notebook Pro");
        product.setPrice(new BigDecimal("3000.00"));
        productRepository.save(product);

        Cache cache = cacheManager.getCache("products");
        assertThat(cache).isNotNull();
        cache.clear();

        clearInvocations(productRepository);

        // GET após limpar o cache precisa consultar o banco novamente
        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook Pro"));

        verify(productRepository, times(1)).findById(product.getId());
    }

    @Test
    @DisplayName("Após limpar o cache, produto removido retorna 404")
    void deleteLimpaCache() throws Exception {
        // ATENÇÃO: este teste NÃO valida o @CacheEvict do DELETE; ele limpa o cache à mão.
        // Popula
        mockMvc.perform(get("/products/" + product.getId()));

        productRepository.deleteAll();

        Cache cache = cacheManager.getCache("products");
        assertThat(cache).isNotNull();
        cache.clear();

        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Listagem cacheada por combinação de filtros")
    void listagemCacheadaPorFiltros() throws Exception {
        // Popula
        mockMvc.perform(get("/products").param("category", "Eletronicos"))
                .andExpect(status().isOk());

        clearInvocations(productRepository);

        // Mesma combinação: deve usar cache
        mockMvc.perform(get("/products").param("category", "Eletronicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Notebook"));

        verify(productRepository, never())
                .findByCategoryIgnoreCase(any(), any());
    }

    @Test
    @DisplayName("Listagem vazia também é cacheada")
    void listagemVaziaTambemCacheada() throws Exception {
        mockMvc.perform(get("/products").param("category", "Inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        clearInvocations(productRepository);

        mockMvc.perform(get("/products").param("category", "Inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(productRepository, never())
                .findByCategoryIgnoreCase(any(), any());
    }

    @Test
    @DisplayName("Caches configurados existem")
    void cachesConfiguradosExistem() {
        assertThat(cacheManager.getCache("products")).isNotNull();
        assertThat(cacheManager.getCache("productLists")).isNotNull();
    }
}