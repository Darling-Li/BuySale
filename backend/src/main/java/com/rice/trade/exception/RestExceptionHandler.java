package com.rice.trade.exception;

import com.rice.trade.dto.ErrorResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", "请求参数校验失败", details));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("NOT_FOUND", ex.getMessage(), null));
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("BUSINESS_ERROR", ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "系统异常，请稍后重试", ex.getMessage()));
    }
}

