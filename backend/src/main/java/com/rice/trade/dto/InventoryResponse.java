package com.rice.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryResponse(
        Long id,
        Long warehouseId,
        String warehouseName,
        String productType,
        String productTypeLabel,
        String productName,
        BigDecimal quantityJin,
        BigDecimal averageCostPerJin,
        LocalDateTime updatedAt
) {
}
