package com.rice.trade.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SystemUserResponse(
        Long id,
        String username,
        String displayName,
        Boolean enabled,
        List<String> roleCodes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
