package com.alvescosta.loja.dto;

import com.alvescosta.loja.domain.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
    public static OrderItemResponse from(OrderItem i) {
        return new OrderItemResponse(
                i.getId(),
                i.getProduct() != null ? i.getProduct().getId() : null,
                i.getProductName(),
                i.getUnitPrice(),
                i.getQuantity(),
                i.getSubtotal()
        );
    }
}
