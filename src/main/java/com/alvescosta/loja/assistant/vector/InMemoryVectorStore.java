package com.alvescosta.loja.assistant.vector;

import com.alvescosta.loja.assistant.embedding.Vectors;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementação de VectorStore em memória (RAM).
 * Ideal para testes e para ambientes de desenvolvimento.
 * Para produção, usar um adapter para pgvector ou outro banco vetorial.
 */
public class InMemoryVectorStore implements VectorStore {

    private final Map<String, SearchResult> store = new ConcurrentHashMap<>();

    @Override
    public void upsert(String id, float[] vector, Map<String, String> metadata) {
        // No upsert, o score é inicializado com 0. Ele só é calculado na busca.
        store.put(id, new SearchResult(id, vector, metadata, 0.0));
    }

    @Override
    public List<SearchResult> search(float[] queryVector, int topK) {
        return store.values().stream()
                .map(item -> new SearchResult(
                        item.id(),
                        item.vector(),
                        item.metadata(),
                        Vectors.similaridadeCosseno(queryVector, item.vector())
                ))
                .sorted(Comparator.comparingDouble(SearchResult::score).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    @Override
    public void clear() {
        store.clear();
    }
}