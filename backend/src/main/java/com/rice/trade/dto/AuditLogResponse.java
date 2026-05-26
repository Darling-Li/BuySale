package com.rice.trade.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String username,
        String roleName,
        String method,
        String path,
        String actionName,
        String requestParams,
        Integer statusCode,
        String ipAddress,
        String userAgent,
        LocalDateTime occurredAt
) {
}
