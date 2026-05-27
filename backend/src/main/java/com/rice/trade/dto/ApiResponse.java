package com.rice.trade.dto;

public record ApiResponse<T>(
        int code,
        T data,
        String msg
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, data, "success");
    }

    public static ApiResponse<Object> failure(int code, String msg, Object data) {
        return new ApiResponse<>(code, data, msg);
    }
}
