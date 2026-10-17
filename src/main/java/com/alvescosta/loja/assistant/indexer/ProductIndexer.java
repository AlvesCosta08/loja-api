package com.alvescosta.loja.assistant.indexer;

import com.alvescosta.loja.assistant.embedding.EmbeddingProvider;
import com.alvescosta.loja.assistant.vector.VectorStore;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProductIndexer {

    private final EmbeddingProvider embeddingProvider;
    private final VectorStore vectorStore;

    public ProductIndexer(EmbeddingProvider embeddingProvider, VectorStore vectorStore) {
        this.embeddingProvider = embeddingProvider;
        this.vectorStore = vectorStore;
    }

    @EventListener
    public void onProductCreated(ProductEvent event) {
        if (!event.active()) {
            return;
        }
        
        // Gera o embedding a partir do nome do produto
        float[] vector = embeddingProvider.embed(event.name());
        
        // Metadados para facilitar o filtro/contexto na busca
        Map<String, String> metadata = Map.of(
            "name", event.name(),
            "category", event.category() != null ? event.category() : "Sem categoria"
        );
        
        // Upsert no VectorStore com ID prefixado
        vectorStore.upsert("product:" + event.id(), vector, metadata);
    }

    @EventListener
    public void onProductDeleted(ProductEvent event) {
        // Apenas remove do índice, sem gerar embedding
        vectorStore.delete("product:" + event.id());
    }
}