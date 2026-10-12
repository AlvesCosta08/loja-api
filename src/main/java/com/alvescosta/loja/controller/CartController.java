package com.alvescosta.loja.controller;

import com.alvescosta.loja.dto.AddToCartRequest;
import com.alvescosta.loja.dto.CartResponse;
import com.alvescosta.loja.dto.UpdateCartItemRequest;
import com.alvescosta.loja.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItem(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AddToCartRequest request) {
        return cartService.addItem(currentUserId(principal), request);
    }

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal UserDetails principal) {
        return cartService.getCart(currentUserId(principal));
    }

    @PutMapping("/items/{id}")
    public CartResponse updateQuantity(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateQuantity(currentUserId(principal), id, request.quantity());
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        cartService.removeItem(currentUserId(principal), id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@AuthenticationPrincipal UserDetails principal) {
        cartService.clearCart(currentUserId(principal));
    }

    private Long currentUserId(UserDetails principal) {
        return Long.parseLong(principal.getUsername());
    }
}
