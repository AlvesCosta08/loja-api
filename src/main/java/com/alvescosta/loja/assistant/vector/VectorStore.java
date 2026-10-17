package com.alvescosta.loja.assistant.vector;

import java.util.List;
import java.util.Map;

public interface VectorStore {

    /**
     * Insere ou atualiza um vetor no store.
     * @param id Identificador único (ex: "product:42")
     * @param vector O vetor float[] gerado pelo EmbeddingProvider
     * @param metadata Metadados associados (ex: nome, categoria)
     */
    void upsert(String id, float[] vector, Map<String, String> metadata);

    /**
     * Busca os top-K vetores mais similares ao vetor de consulta.
     * @param queryVector O vetor da busca
     * @param topK Quantidade máxima de resultados
     * @return Lista de resultados ordenados por similaridade (maior para menor)
     */
    List<SearchResult> search(float[] queryVector, int topK);

    /**
     * Remove um vetor pelo ID.
     */
    void delete(String id);

    /**
     * Limpa todos os vetores do store.
     */
    void clear();
}