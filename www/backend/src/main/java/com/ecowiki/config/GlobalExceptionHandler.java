package com.ecowiki.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ecowiki.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        // 过滤敏感信息
        String safeMessage = getSafeErrorMessage(e.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(400, safeMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        // 记录错误但不暴露给客户端
        System.err.println("系统错误: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(500, "系统繁忙，请稍后重试"));
    }

    private String getSafeErrorMessage(String originalMessage) {
        // 过滤可能的敏感信息
        if (originalMessage.contains("database") || originalMessage.contains("SQL")) {
            return "数据处理失败";
        }
        return originalMessage;
    }
}