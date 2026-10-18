package com.alvescosta.loja.controller;

import com.alvescosta.loja.dto.PaymentResponse;
import com.alvescosta.loja.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPix(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Long orderId) {
        return paymentService.createPix(
                orderId, currentUserId(principal), isAdmin(principal));
    }

    @PostMapping("/card")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createCard(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Long orderId) {
        return paymentService.createCard(
                orderId, currentUserId(principal), isAdmin(principal));
    }

    @GetMapping("/{id}")
    public PaymentResponse findById(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        return paymentService.findByIdForUser(
                id, currentUserId(principal), isAdmin(principal));
    }

    /**
     * Busca o pagamento mais recente do pedido. Endpoint F5-safe:
     * o frontend chama isso ao montar a tela de pagamento para reidratar
     * o QR Code / Pix copia-e-cola após reload, sem precisar criar
     * um novo pagamento.
     *
     * Retorna 404 quando o pedido não tem nenhum pagamento ainda.
     */
    @GetMapping
    public PaymentResponse findByOrder(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Long orderId) {
        return paymentService.findLatestByOrder(
                orderId, currentUserId(principal), isAdmin(principal));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponse confirm(@PathVariable Long id) {
        return paymentService.confirm(id);
    }

    private Long currentUserId(UserDetails principal) {
        return Long.parseLong(principal.getUsername());
    }

    private boolean isAdmin(UserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}