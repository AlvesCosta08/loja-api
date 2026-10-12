package com.alvescosta.loja.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        long totalOrders,
        BigDecimal totalRevenue,
        BigDecimal averageTicket
) {}
