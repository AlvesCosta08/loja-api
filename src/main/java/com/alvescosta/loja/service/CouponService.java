package com.alvescosta.loja.service;

import com.alvescosta.loja.domain.Coupon;
import com.alvescosta.loja.domain.DiscountType;
import com.alvescosta.loja.dto.CouponRequest;
import com.alvescosta.loja.dto.CouponResponse;
import com.alvescosta.loja.exception.BusinessException;
import com.alvescosta.loja.exception.NotFoundException;
import com.alvescosta.loja.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public CouponResponse create(CouponRequest request) {
        if (couponRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessException("Codigo de cupom ja existe: " + request.code());
        }
        if (request.type() == DiscountType.PERCENTAGE
                && request.value().compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("Percentual nao pode ser maior que 100");
        }

        Coupon c = Coupon.builder()
                .code(request.code().toUpperCase())
                .type(request.type())
                .value(request.value())
                .minOrderValue(request.minOrderValue())
                .maxUses(request.maxUses())
                .usageCount(0)
                .expiresAt(request.expiresAt())
                .active(true)
                .build();

        return CouponResponse.from(couponRepository.save(c));
    }

    /**
     * Valida o cupom e retorna o valor do desconto a aplicar sobre o subtotal.
     * Não incrementa usageCount — isso é feito no checkout após criar o pedido.
     */
    @Transactional(readOnly = true)
    public BigDecimal validateAndCalculate(String code, BigDecimal subtotal) {
        Coupon c = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException("Cupom inexistente: " + code));

        if (!Boolean.TRUE.equals(c.getActive())) {
            throw new BusinessException("Cupom inativo");
        }
        if (c.getExpiresAt() != null && c.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Cupom expirado");
        }
        if (c.getMaxUses() != null && c.getUsageCount() >= c.getMaxUses()) {
            throw new BusinessException("Cupom esgotado");
        }
        if (c.getMinOrderValue() != null
                && subtotal.compareTo(c.getMinOrderValue()) < 0) {
            throw new BusinessException(
                    "Valor minimo nao atingido: " + c.getMinOrderValue());
        }

        BigDecimal discount = (c.getType() == DiscountType.PERCENTAGE)
                ? subtotal.multiply(c.getValue())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : c.getValue();

        // Desconto nunca maior que subtotal
        return discount.min(subtotal);
    }

    @Transactional
    public void registerUsage(String code) {
        couponRepository.findByCodeIgnoreCase(code).ifPresent(c -> {
            c.setUsageCount(c.getUsageCount() + 1);
            couponRepository.save(c);
        });
    }

    @Transactional(readOnly = true)
    public CouponResponse findByCode(String code) {
        return couponRepository.findByCodeIgnoreCase(code)
                .map(CouponResponse::from)
                .orElseThrow(() -> new NotFoundException("Cupom nao encontrado"));
    }
}
