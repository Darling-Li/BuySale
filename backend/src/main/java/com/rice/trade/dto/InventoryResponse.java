package com.rice.trade.dto;

import com.rice.trade.enums.ProductType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryResponse(
        Long id,
        Long warehouseId,
        String warehouseName,
        ProductType productType,
        String productTypeLabel,
        String productName,
        BigDecimal quantityJin,
        BigDecimal averageCostPerJin,
        LocalDateTime updatedAt
) {
}

