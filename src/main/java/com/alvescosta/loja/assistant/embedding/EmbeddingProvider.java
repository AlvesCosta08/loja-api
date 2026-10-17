package com.alvescosta.loja.assistant.embedding;

import java.util.List;

public interface EmbeddingProvider {

    float[] embed(String text);

    default List<float[]> embedAll(List<String> texts) {
        return texts.stream().map(this::embed).toList();
    }
}