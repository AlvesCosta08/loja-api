package com.alvescosta.loja.controller;

import com.alvescosta.loja.dto.ShippingQuoteRequest;
import com.alvescosta.loja.dto.ShippingQuoteResponse;
import com.alvescosta.loja.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/quote")
    public ShippingQuoteResponse quote(@Valid @RequestBody ShippingQuoteRequest request) {
        return shippingService.quote(request.cep(), request.subtotal());
    }
}
