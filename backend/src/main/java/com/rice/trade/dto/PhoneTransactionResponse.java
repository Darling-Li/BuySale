package com.rice.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PhoneTransactionResponse(
        String businessType,
        String businessTypeLabel,
        Long businessId,
        LocalDateTime transactionDate,
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
        BigDecimal settledAmount,
        BigDecimal unsettledAmount,
        String settlementChannels,
        String remark,
        LocalDateTime createdAt
) {
}
