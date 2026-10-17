package com.alvescosta.loja.dto;

import com.alvescosta.loja.domain.Product;

import java.io.Serializable; // <-- IMPORT ADICIONADO
import java.math.BigDecimal;

// Adicionado "implements Serializable" no final
public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stock,
        String category,
        String imageUrl
) implements Serializable { 

    // Necessário para a serialização
    private static final long serialVersionUID = 1L;

    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getStock(),
                p.getCategory(),
                p.getImageUrl()
        );
    }
}