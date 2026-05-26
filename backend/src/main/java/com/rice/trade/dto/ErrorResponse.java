package com.rice.trade.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String code,
        String message,
        Object details,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String code, String message, Object details) {
        return new ErrorResponse(code, message, details, LocalDateTime.now());
    }
}

