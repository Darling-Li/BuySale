package com.rice.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PurchaseResponse(
        Long id,
        String productType,
        String productTypeLabel,
        String productName,
        Long warehouseId,
        String warehouseName,
        String counterpartyName,
        String counterpartyPhone,
        String counterpartyProvince,
        String counterpartyCity,
        String counterpartyCounty,
        String counterpartyAddressDetail,
        String counterpartyAddressDisplay,
        BigDecimal quantity,
        String unitName,
        BigDecimal unitToJin,
        BigDecimal unitPrice,
        BigDecimal weightJin,
        BigDecimal pricePerJin,
        BigDecimal totalAmount,
        LocalDate purchasedAt,
        String remark,
        LocalDateTime createdAt
) {
}
