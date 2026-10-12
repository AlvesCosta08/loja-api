package com.alvescosta.loja.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ShippingQuoteRequest(
        @NotBlank String cep,
        @NotNull @PositiveOrZero BigDecimal subtotal
) {}
