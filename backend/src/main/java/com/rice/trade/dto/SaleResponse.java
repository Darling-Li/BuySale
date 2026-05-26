package com.rice.trade.dto;

import com.rice.trade.enums.ProductType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SaleResponse(
        Long id,
        ProductType productType,
        String productTypeLabel,
        String productName,
        Long warehouseId,
        String warehouseName,
        String buyerName,
        String buyerPhone,
        String buyerAddress,
        BigDecimal quantity,
        String unitName,
        BigDecimal unitToJin,
        BigDecimal unitPrice,
        BigDecimal weightJin,
        BigDecimal pricePerJin,
        BigDecimal totalAmount,
        boolean settled,
        LocalDate soldAt,
        String remark,
        LocalDateTime createdAt
) {
}
