package com.alvescosta.loja.controller;

import com.alvescosta.loja.dto.AddToCartRequest;
import com.alvescosta.loja.dto.CartResponse;
import com.alvescosta.loja.dto.UpdateCartItemRequest;
import com.alvescosta.loja.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItem(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AddToCartRequest request) {
        return cartService.addItem(currentUserId(userId), request);
    }

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal String userId) {
        return cartService.getCart(currentUserId(userId));
    }

    @PutMapping("/items/{id}")
    public CartResponse updateQuantity(
            @AuthenticationPrincipal String userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateQuantity(currentUserId(userId), id, request.quantity());
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(
            @AuthenticationPrincipal String userId,
            @PathVariable Long id) {
        cartService.removeItem(currentUserId(userId), id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@AuthenticationPrincipal String userId) {
        cartService.clearCart(currentUserId(userId));
    }

    // Método auxiliar limpo e seguro
    private Long currentUserId(String userId) {
        return Long.parseLong(userId);
    }
}