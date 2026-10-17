package com.alvescosta.loja.assistant.retrieval;

import com.alvescosta.loja.assistant.embedding.EmbeddingProvider;
import com.alvescosta.loja.assistant.vector.SearchResult;
import com.alvescosta.loja.assistant.vector.VectorStore;

import java.util.List;

public class VectorRetriever {

    private final EmbeddingProvider embeddingProvider;
    private final VectorStore vectorStore;
    private final double threshold;

    public VectorRetriever(EmbeddingProvider embeddingProvider, VectorStore vectorStore, double threshold) {
        this.embeddingProvider = embeddingProvider;
        this.vectorStore = vectorStore;
        this.threshold = threshold;
    }

    /**
     * Busca os top-K resultados mais relevantes para uma query, filtrando por similaridade mínima.
     *
     * Estratégia: pedimos ao store mais resultados do que o topK desejado (ex: topK * 2),
     * pois parte deles pode ser descartada pelo threshold. Depois filtramos e limitamos ao topK.
     */
    public List<SearchResult> retrieve(String query, int topK) {
        float[] queryVector = embeddingProvider.embed(query);

        // Pede mais resultados ao store para compensar o filtro
        int fetchLimit = topK * 2;
        List<SearchResult> candidates = vectorStore.search(queryVector, fetchLimit);

        return candidates.stream()
                .filter(r -> r.score() >= threshold)
                .limit(topK)
                .toList();
    }
}