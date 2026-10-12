package com.alvescosta.loja.dto;

import com.alvescosta.loja.domain.Role;

import java.math.BigDecimal;

public record AdminUserResponse(
        Long id,
        String email,
        String name,
        Role role,
        long orderCount,
        BigDecimal totalSpent
) {}
