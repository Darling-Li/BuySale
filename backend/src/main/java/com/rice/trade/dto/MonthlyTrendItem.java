package com.rice.trade.dto;

import java.math.BigDecimal;

public record MonthlyTrendItem(
        int month,
        BigDecimal purchaseWeightJin,
        BigDecimal purchaseAmount,
        BigDecimal saleWeightJin,
        BigDecimal saleAmount,
        BigDecimal saleYoYRate,
        BigDecimal saleMoMRate
) {
}

