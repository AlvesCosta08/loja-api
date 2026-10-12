package com.alvescosta.loja.service;

import com.alvescosta.loja.domain.*;
import com.alvescosta.loja.dto.OrderResponse;
import com.alvescosta.loja.exception.BusinessException;
import com.alvescosta.loja.exception.NotFoundException;
import com.alvescosta.loja.repository.CartItemRepository;
import com.alvescosta.loja.repository.OrderRepository;
import com.alvescosta.loja.repository.ProductRepository;
import com.alvescosta.loja.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;
    private final ShippingService shippingService;

    @Transactional
    public OrderResponse checkout(Long userId, String couponCode, String cep) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException("Carrinho vazio");
        }

        for (CartItem ci : cartItems) {
            if (ci.getQuantity() > ci.getProduct().getStock()) {
                throw new BusinessException(
                        "Estoque insuficiente para: " + ci.getProduct().getName());
            }
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .total(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .shippingCost(BigDecimal.ZERO)
                .couponCode(couponCode)
                .items(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();
            BigDecimal line = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));

            order.getItems().add(OrderItem.builder()
                    .order(order)
                    .product(p)
                    .productName(p.getName())
                    .unitPrice(p.getPrice())
                    .quantity(ci.getQuantity())
                    .subtotal(line)
                    .build());

            subtotal = subtotal.add(line);
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.isBlank()) {
            discount = couponService.validateAndCalculate(couponCode, subtotal);
            order.setDiscount(discount);
        }

        BigDecimal shippingCost = BigDecimal.ZERO;
        if (cep != null && !cep.isBlank()) {
            shippingCost = shippingService.costFor(cep, subtotal, ShippingType.STANDARD);
            order.setShippingCost(shippingCost);
        }

        order.setTotal(subtotal.subtract(discount).add(shippingCost));

        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();
            p.setStock(p.getStock() - ci.getQuantity());
            productRepository.save(p);
        }

        orderRepository.save(order);

        if (couponCode != null && !couponCode.isBlank()) {
            couponService.registerUsage(couponCode);
        }

        cartItemRepository.deleteAll(cartItems);

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listMine(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse findByIdForUser(Long orderId, Long userId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Pedido nao encontrado"));

        if (!isAdmin && !order.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Acesso negado");
        }
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Pedido nao encontrado"));
        order.setStatus(newStatus);
        orderRepository.save(order);
        return OrderResponse.from(order);
    }
}
