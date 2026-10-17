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
    public void onProductEvent(ProductEvent event) {
        if (event.active()) {
            index(event);
        } else {
            remove(event);
        }
    }

    private void index(ProductEvent event) {
        float[] vector = embeddingProvider.embed(event.name());
        Map<String, String> metadata = Map.of(
            "name", event.name(),
            "category", event.category() != null ? event.category() : "Sem categoria"
        );
        vectorStore.upsert("product:" + event.id(), vector, metadata);
    }

    private void remove(ProductEvent event) {
        vectorStore.delete("product:" + event.id());
    }
}