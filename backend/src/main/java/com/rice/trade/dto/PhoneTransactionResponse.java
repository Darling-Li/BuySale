package com.rice.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PhoneTransactionResponse(
        String businessType,
        String businessTypeLabel,
        Long businessId,
        LocalDate transactionDate,
        String productType,
        String productTypeLabel,
        String productName,
        Long warehouseId,
        String warehouseName,
        String contactName,
        String contactPhone,
        String contactAddress,
        BigDecimal quantity,
        String unitName,
        BigDecimal unitToJin,
        BigDecimal unitPrice,
        BigDecimal weightJin,
        BigDecimal pricePerJin,
        BigDecimal totalAmount,
        Boolean settled,
        String remark,
        LocalDateTime createdAt
) {
}
