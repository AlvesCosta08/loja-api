package com.alvescosta.loja.dto;

import com.alvescosta.loja.domain.Coupon;
import com.alvescosta.loja.domain.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;

public record CouponResponse(
        Long id,
        String code,
        DiscountType type,
        BigDecimal value,
        BigDecimal minOrderValue,
        Integer maxUses,
        Integer usageCount,
        Instant expiresAt,
        Boolean active
) {
    public static CouponResponse from(Coupon c) {
        return new CouponResponse(
                c.getId(), c.getCode(), c.getType(), c.getValue(),
                c.getMinOrderValue(), c.getMaxUses(), c.getUsageCount(),
                c.getExpiresAt(), c.getActive()
        );
    }
}
