package com.rice.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
        Long id,
        String productType,
        String productTypeLabel,
        String productName,
        Long warehouseId,
        String warehouseName,
        String buyerName,
        String buyerPhone,
        String buyerProvince,
        String buyerCity,
        String buyerCounty,
        String buyerAddressDetail,
        String buyerAddressDisplay,
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
        List<SaleSettlementResponse> settlements,
        LocalDate soldAt,
        String remark,
        LocalDateTime createdAt
) {
}
