package com.alvescosta.loja.service;

import com.alvescosta.loja.dto.CartItemResponse;
import com.alvescosta.loja.dto.CartResponse;

import java.math.BigDecimal;
import java.util.List;

public final class CartCalculator {

    private CartCalculator() {}

    public static CartResponse build(List<CartItemResponse> items) {
        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int itemCount = items.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        return new CartResponse(items, total, itemCount);
    }
}
