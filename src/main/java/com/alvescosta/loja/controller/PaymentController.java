package com.alvescosta.loja.controller;

import com.alvescosta.loja.dto.PaymentResponse;
import com.alvescosta.loja.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPix(
            @AuthenticationPrincipal String userId,
            Authentication authentication,
            @RequestParam Long orderId) {
        return paymentService.createPix(
                orderId, currentUserId(userId), isAdmin(authentication));
    }

    @PostMapping("/card")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createCard(
            @AuthenticationPrincipal String userId,
            Authentication authentication,
            @RequestParam Long orderId) {
        return paymentService.createCard(
                orderId, currentUserId(userId), isAdmin(authentication));
    }

    @GetMapping("/{id}")
    public PaymentResponse findById(
            @AuthenticationPrincipal String userId,
            Authentication authentication,
            @PathVariable Long id) {
        return paymentService.findByIdForUser(
                id, currentUserId(userId), isAdmin(authentication));
    }

    @GetMapping
    public PaymentResponse findByOrder(
            @AuthenticationPrincipal String userId,
            Authentication authentication,
            @RequestParam Long orderId) {
        return paymentService.findLatestByOrder(
                orderId, currentUserId(userId), isAdmin(authentication));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponse confirm(@PathVariable Long id) {
        return paymentService.confirm(id);
    }

    private Long currentUserId(String userId) {
        return Long.parseLong(userId);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}