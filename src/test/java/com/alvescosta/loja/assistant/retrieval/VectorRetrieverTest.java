package com.alvescosta.loja.assistant.retrieval;

import com.alvescosta.loja.assistant.embedding.EmbeddingProvider;
import com.alvescosta.loja.assistant.vector.SearchResult;
import com.alvescosta.loja.assistant.vector.VectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class VectorRetrieverTest {

    private EmbeddingProvider embeddingProvider;
    private VectorStore vectorStore;
    private VectorRetriever retriever;

    @BeforeEach
    void setUp() {
        embeddingProvider = mock(EmbeddingProvider.class);
        vectorStore = mock(VectorStore.class);
        // Threshold de 0.5: resultados com score abaixo disso serão descartados
        retriever = new VectorRetriever(embeddingProvider, vectorStore, 0.5);
    }

    @Test
    void deve_retornar_vazio_se_nao_houver_resultados_no_store() {
        when(embeddingProvider.embed("pergunta")).thenReturn(new float[]{1, 0});
        when(vectorStore.search(any(), anyInt())).thenReturn(List.of());

        List<SearchResult> results = retriever.retrieve("pergunta", 5);

        assertThat(results).isEmpty();
    }

    @Test
    void deve_filtrar_resultados_abaixo_do_threshold() {
        float[] queryVector = {1, 0};
        when(embeddingProvider.embed("pergunta")).thenReturn(queryVector);
        
        // Simula o retorno do store (um acima do threshold, um abaixo)
        SearchResult bom = new SearchResult("p1", new float[]{1, 0}, Map.of(), 0.9);
        SearchResult ruim = new SearchResult("p2", new float[]{0, 1}, Map.of(), 0.3);
        
        // O store retorna ambos, o retriever deve filtrar e manter apenas "bom"
        when(vectorStore.search(queryVector, 10)).thenReturn(List.of(bom, ruim));

        List<SearchResult> results = retriever.retrieve("pergunta", 5);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo("p1");
    }

    @Test
    void deve_chamar_embedding_provider_e_vector_store() {
        when(embeddingProvider.embed("pergunta")).thenReturn(new float[]{1, 0});
        when(vectorStore.search(any(), anyInt())).thenReturn(List.of());

        retriever.retrieve("pergunta", 5);

        verify(embeddingProvider).embed("pergunta");
        // O retriever deve pedir mais resultados ao store (ex: topK * 2) para compensar o filtro
        verify(vectorStore).search(any(), eq(10)); 
    }
}