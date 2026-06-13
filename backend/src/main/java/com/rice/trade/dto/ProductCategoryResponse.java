package com.rice.trade.dto;

import java.time.LocalDateTime;

public record ProductCategoryResponse(
        Long id,
        String code,
        String name,
        boolean systemBuiltin,
        Integer sortOrder,
        boolean enabled,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
