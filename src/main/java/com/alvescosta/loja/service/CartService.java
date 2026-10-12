package com.alvescosta.loja.service;

import com.alvescosta.loja.domain.CartItem;
import com.alvescosta.loja.domain.Product;
import com.alvescosta.loja.domain.User;
import com.alvescosta.loja.dto.AddToCartRequest;
import com.alvescosta.loja.dto.CartItemResponse;
import com.alvescosta.loja.dto.CartResponse;
import com.alvescosta.loja.exception.BusinessException;
import com.alvescosta.loja.exception.NotFoundException;
import com.alvescosta.loja.repository.CartItemRepository;
import com.alvescosta.loja.repository.ProductRepository;
import com.alvescosta.loja.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponse addItem(Long userId, AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new NotFoundException("Produto nao encontrado"));

        Optional<CartItem> existing =
                cartItemRepository.findByUserIdAndProductId(userId, product.getId());

        int newQuantity = request.quantity()
                + existing.map(CartItem::getQuantity).orElse(0);

        if (newQuantity > product.getStock()) {
            throw new BusinessException(
                    "Quantidade solicitada excede o estoque disponivel");
        }

        CartItem item = existing
                .map(it -> {
                    it.setQuantity(newQuantity);
                    return it;
                })
                .orElseGet(() -> CartItem.builder()
                        .user(user)
                        .product(product)
                        .quantity(newQuantity)
                        .build());

        cartItemRepository.save(item);
        return getCart(userId);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        List<CartItemResponse> items = cartItemRepository.findByUserId(userId)
                .stream()
                .map(CartItemResponse::from)
                .toList();

        return CartCalculator.build(items);
    }

    @Transactional
    public CartResponse updateQuantity(Long userId, Long itemId, int quantity) {
        CartItem item = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Item nao encontrado no carrinho"));

        if (quantity > item.getProduct().getStock()) {
            throw new BusinessException(
                    "Quantidade solicitada excede o estoque disponivel");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return getCart(userId);
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Item nao encontrado no carrinho"));
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
