package com.alvescosta.loja.dto;

import java.util.List;

public record ShippingQuoteResponse(
        String cep,
        List<ShippingOption> options
) {}
