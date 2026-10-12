package com.alvescosta.loja.dto;

import com.alvescosta.loja.domain.Order;
import com.alvescosta.loja.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        OrderStatus status,
        BigDecimal total,
        BigDecimal discount,
        BigDecimal shippingCost,
        String couponCode,
        Instant createdAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getUser().getId(),
                o.getStatus(),
                o.getTotal(),
                o.getDiscount(),
                o.getShippingCost(),
                o.getCouponCode(),
                o.getCreatedAt(),
                o.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}
