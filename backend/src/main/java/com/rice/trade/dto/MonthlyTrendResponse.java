package com.rice.trade.dto;

import com.rice.trade.enums.ProductType;
import java.util.List;

public record MonthlyTrendResponse(
        int year,
        ProductType productType,
        String productTypeLabel,
        List<MonthlyTrendItem> items
) {
}

