package com.rice.trade.dto;

import java.time.LocalDateTime;

public record SystemRoleResponse(
        Long id,
        String code,
        String name,
        String description,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
