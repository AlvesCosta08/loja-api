package com.alvescosta.loja.assistant.vector;

import java.util.Map;

public record SearchResult(
    String id,
    float[] vector,
    Map<String, String> metadata,
    double score
) {}