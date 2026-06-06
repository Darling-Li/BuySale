package com.rice.trade.dto;

import java.util.List;

public record MonthlyTrendResponse(
        int year,
        String productType,
        String productTypeLabel,
        List<MonthlyTrendItem> items
) {
}
