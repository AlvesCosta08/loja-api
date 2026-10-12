package com.alvescosta.loja.controller;

import com.alvescosta.loja.dto.OrderResponse;
import com.alvescosta.loja.dto.UpdateOrderStatusRequest;
import com.alvescosta.loja.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false) String cep) {
        return orderService.checkout(currentUserId(principal), couponCode, cep);
    }

    @GetMapping
    public Page<OrderResponse> listMine(
            @AuthenticationPrincipal UserDetails principal,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return orderService.listMine(currentUserId(principal), pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {
        boolean isAdmin = isAdmin(principal);
        return orderService.findByIdForUser(id, currentUserId(principal), isAdmin);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    private Long currentUserId(UserDetails principal) {
        return Long.parseLong(principal.getUsername());
    }

    private boolean isAdmin(UserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
