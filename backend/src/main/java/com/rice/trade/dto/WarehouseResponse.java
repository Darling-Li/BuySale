package com.rice.trade.dto;

import java.time.LocalDateTime;

public record WarehouseResponse(
        Long id,
        String name,
        String address,
        String contactName,
        String contactPhone,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

