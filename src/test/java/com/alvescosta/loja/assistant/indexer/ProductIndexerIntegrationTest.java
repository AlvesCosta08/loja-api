package com.alvescosta.loja.assistant.indexer;

import com.alvescosta.loja.assistant.embedding.EmbeddingProvider;
import com.alvescosta.loja.assistant.vector.InMemoryVectorStore;
import com.alvescosta.loja.assistant.vector.VectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class ProductIndexerIntegrationTest {

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        public EmbeddingProvider testEmbeddingProvider() {
            EmbeddingProvider mock = mock(EmbeddingProvider.class);
            when(mock.embed(anyString())).thenReturn(new float[]{1, 0});
            return mock;
        }

        @Bean
        @Primary
        public VectorStore testVectorStore() {
            return new InMemoryVectorStore();
        }
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private VectorStore vectorStore;

    @BeforeEach
    void setUp() {
        vectorStore.clear();
    }

    @Test
    void deve_manter_produto_no_indice_apos_criacao() {
        // Arrange
        var evento = new ProductEvent(1L, "Camiseta", "Roupas", true);

        // Act
        eventPublisher.publishEvent(evento);

        // Assert
        var resultados = vectorStore.search(new float[]{1, 0}, 10);
        assertThat(resultados)
            .as("O produto deve estar no índice após a criação")
            .isNotEmpty();
        assertThat(resultados.get(0).id()).isEqualTo("product:1");
    }

    @Test
    void deve_remover_produto_do_indice_apos_delecao() {
        // Arrange: primeiro indexa manualmente para garantir o estado inicial
        vectorStore.upsert("product:1", new float[]{1, 0}, Map.of());
        var evento = new ProductEvent(1L, "Camiseta", "Roupas", false);

        // Act
        eventPublisher.publishEvent(evento);

        // Assert
        var resultados = vectorStore.search(new float[]{1, 0}, 10);
        assertThat(resultados)
            .as("O produto deve ser removido do índice após a deleção")
            .isEmpty();
    }
}