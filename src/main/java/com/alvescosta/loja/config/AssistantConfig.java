package com.alvescosta.loja.config;

import com.alvescosta.loja.assistant.embedding.EmbeddingProvider;
import com.alvescosta.loja.assistant.embedding.FakeEmbeddingProvider;
import com.alvescosta.loja.assistant.vector.InMemoryVectorStore;
import com.alvescosta.loja.assistant.vector.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssistantConfig {

    /**
     * Provider de embedding temporário (fake, determinístico).
     * Será substituído por OpenAiEmbeddingProvider na sessão 2 do RAG.
     */
    @Bean
    public EmbeddingProvider embeddingProvider() {
        return new FakeEmbeddingProvider(1536);
    }

    /**
     * VectorStore em memória.
     * Será substituído por PgVectorStore quando a migração V10 entrar.
     */
    @Bean
    public VectorStore vectorStore() {
        return new InMemoryVectorStore();
    }
}