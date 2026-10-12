package com.alvescosta.loja.dto;

import com.alvescosta.loja.domain.ShippingType;

import java.math.BigDecimal;

public record ShippingOption(
        ShippingType type,
        String label,
        BigDecimal price
) {}
