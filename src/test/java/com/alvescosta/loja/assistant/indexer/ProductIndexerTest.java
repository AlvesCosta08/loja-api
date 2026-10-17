package com.alvescosta.loja.assistant.indexer;

import com.alvescosta.loja.assistant.embedding.EmbeddingProvider;
import com.alvescosta.loja.assistant.vector.VectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductIndexerTest {

    private EmbeddingProvider embeddingProvider;
    private VectorStore vectorStore;
    private ProductIndexer indexer;

    @BeforeEach
    void setUp() {
        embeddingProvider = mock(EmbeddingProvider.class);
        vectorStore = mock(VectorStore.class);
        indexer = new ProductIndexer(embeddingProvider, vectorStore);
    }

    @Test
    void deve_indexar_produto_ao_receber_evento_de_criacao() {
        // Arrange
        when(embeddingProvider.embed("Camiseta de algodão")).thenReturn(new float[]{1, 0});
        var evento = new ProductEvent(1L, "Camiseta de algodão", "Roupas", true);

        // Act
        indexer.onProductCreated(evento);

        // Assert
        verify(embeddingProvider).embed("Camiseta de algodão");
        verify(vectorStore).upsert(eq("product:1"), eq(new float[]{1, 0}), anyMap());
    }

    @Test
    @SuppressWarnings("unchecked")
    void deve_incluir_metadados_no_vetor_indexado() {
        // Arrange
        when(embeddingProvider.embed(anyString())).thenReturn(new float[]{1, 0});
        var evento = new ProductEvent(42L, "Notebook", "Eletrônicos", true);

        // Act
        indexer.onProductCreated(evento);

        // Assert
        ArgumentCaptor<Map<String, String>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(vectorStore).upsert(eq("product:42"), any(), metadataCaptor.capture());

        Map<String, String> metadata = metadataCaptor.getValue();
        assertThat(metadata).containsEntry("name", "Notebook");
        assertThat(metadata).containsEntry("category", "Eletrônicos");
    }

    @Test
    void deve_remover_produto_do_indice_ao_receber_evento_de_delecao() {
        // Arrange
        var evento = new ProductEvent(1L, "Camiseta", "Roupas", false);

        // Act
        indexer.onProductDeleted(evento);

        // Assert
        verify(vectorStore).delete("product:1");
        verifyNoInteractions(embeddingProvider); // Não deve gerar embedding ao deletar
    }
}