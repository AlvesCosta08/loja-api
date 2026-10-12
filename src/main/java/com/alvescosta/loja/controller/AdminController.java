package com.alvescosta.loja.controller;

import com.alvescosta.loja.dto.*;
import com.alvescosta.loja.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/orders")
    public Page<OrderResponse> listAllOrders(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return adminService.listAllOrders(pageable);
    }

    @GetMapping("/users")
    public List<AdminUserResponse> listUsers() {
        return adminService.listUsersWithStats();
    }

    @PatchMapping("/users/{id}/role")
    public UserResponse updateRole(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return adminService.updateUserRole(id, currentUserId(principal), request.role());
    }

    @GetMapping("/products/low-stock")
    public List<ProductResponse> lowStock(
            @RequestParam(defaultValue = "5") int threshold) {
        return adminService.lowStock(threshold);
    }

    @PostMapping("/products/{id}/restock")
    public ProductResponse restock(
            @PathVariable Long id,
            @Valid @RequestBody RestockRequest request) {
        return adminService.restock(id, request.quantity());
    }

    private Long currentUserId(UserDetails principal) {
        return Long.parseLong(principal.getUsername());
    }
}
