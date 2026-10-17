package com.alvescosta.loja.assistant.indexer;

public record ProductEvent(
    Long id,
    String name,
    String category,
    boolean active
) {}