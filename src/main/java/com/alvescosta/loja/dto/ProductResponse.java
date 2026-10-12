package com.alvescosta.loja.dto;

import com.alvescosta.loja.domain.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stock,
        String category,
        String imageUrl
) {
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
