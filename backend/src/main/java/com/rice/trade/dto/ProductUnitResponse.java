package com.rice.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductUnitResponse(
        Long id,
        String name,
        BigDecimal unitToJin,
        boolean systemBuiltin,
        Integer sortOrder,
        boolean enabled,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
