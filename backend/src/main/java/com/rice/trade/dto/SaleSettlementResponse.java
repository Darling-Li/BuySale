package com.rice.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleSettlementResponse(
        Long id,
        Long saleOrderId,
        BigDecimal amount,
        String channel,
        String channelLabel,
        LocalDateTime settledAt,
        String remark,
        LocalDateTime createdAt
) {
}
