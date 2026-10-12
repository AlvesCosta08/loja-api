package com.alvescosta.loja.service;

import com.alvescosta.loja.domain.ShippingType;
import com.alvescosta.loja.dto.ShippingOption;
import com.alvescosta.loja.dto.ShippingQuoteResponse;
import com.alvescosta.loja.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Calcula opcoes de frete com base no CEP de destino e subtotal do carrinho.
 *
 * Regras:
 *  - SP (prefixo 0/1): STANDARD R$ 10 / EXPRESS R$ 20
 *  - RJ/ES (prefixo 2/3): STANDARD R$ 15 / EXPRESS R$ 30
 *  - Outros: STANDARD R$ 20 / EXPRESS R$ 40
 *  - STANDARD fica gratis se subtotal >= R$ 200 / EXPRESS nunca e gratis
 */
@Service
public class ShippingService {

    private static final BigDecimal FREE_THRESHOLD = new BigDecimal("200.00");

    public ShippingQuoteResponse quote(String rawCep, BigDecimal subtotal) {
        String cep = normalizeCep(rawCep);
        validateCep(cep);

        char prefix = cep.charAt(0);
        BigDecimal standardPrice;
        BigDecimal expressPrice;

        if (prefix == '0' || prefix == '1') {
            standardPrice = new BigDecimal("10.00");
            expressPrice  = new BigDecimal("20.00");
        } else if (prefix == '2' || prefix == '3') {
            standardPrice = new BigDecimal("15.00");
            expressPrice  = new BigDecimal("30.00");
        } else {
            standardPrice = new BigDecimal("20.00");
            expressPrice  = new BigDecimal("40.00");
        }

        if (subtotal.compareTo(FREE_THRESHOLD) >= 0) {
            standardPrice = BigDecimal.ZERO;
        }

        return new ShippingQuoteResponse(cep, List.of(
                new ShippingOption(ShippingType.STANDARD, "Entrega padrao", standardPrice),
                new ShippingOption(ShippingType.EXPRESS,  "Entrega expressa", expressPrice)
        ));
    }

    public BigDecimal costFor(String rawCep, BigDecimal subtotal, ShippingType type) {
        return quote(rawCep, subtotal).options().stream()
                .filter(o -> o.type() == type)
                .map(ShippingOption::price)
                .findFirst()
                .orElseThrow(() -> new BusinessException("Tipo de frete invalido"));
    }

    private String normalizeCep(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("[^0-9]", "");
    }

    private void validateCep(String cep) {
        if (cep.length() != 8) {
            throw new BusinessException("CEP invalido: deve conter 8 digitos");
        }
    }
}
