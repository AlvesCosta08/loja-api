package com.alvescosta.loja.dto;

import com.alvescosta.loja.domain.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

public record CouponRequest(
        @NotBlank String code,
        @NotNull DiscountType type,
        @NotNull @Positive BigDecimal value,
        @PositiveOrZero BigDecimal minOrderValue,
        @Positive Integer maxUses,
        Instant expiresAt
) {}
