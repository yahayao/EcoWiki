/**
 * 全局异常处理器
 * 
 * 功能包括：
 * - 统一异常捕获和处理
 * - 标准化API错误响应格式
 * - 系统异常日志记录
 * - 用户友好的错误信息返回
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.util.LoggerUtil;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理运行时异常
     * 
     * 捕获应用程序中抛出的RuntimeException及其子类异常。
     * 这类异常通常是业务逻辑异常或参数验证异常，属于客户端错误。
     * 
     * 处理策略：
     * - 返回400 Bad Request状态码
     * - 过滤敏感信息后返回错误消息
     * - 保留业务相关的错误信息
     * 
     * 常见异常类型：
     * - IllegalArgumentException: 参数异常
     * - IllegalStateException: 状态异常
     * - 自定义业务异常: 业务逻辑验证失败
     * 
     * @param e 运行时异常对象
     * @return 包含错误信息的API响应，状态码400
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        // 记录业务异常日志
        LoggerUtil.warn("业务异常: {}", e.getMessage(), e);
        
        // 过滤敏感信息，确保不向客户端暴露系统内部细节
        String safeMessage = getSafeErrorMessage(e.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(400, safeMessage));
    }

    /**
     * 处理通用异常
     * 
     * 捕获所有未被特定异常处理器处理的Exception类型异常。
     * 这类异常通常是系统级异常，如IO异常、网络异常等，属于服务器错误。
     * 
     * 处理策略：
     * - 返回500 Internal Server Error状态码
     * - 在服务端记录完整错误信息
     * - 向客户端返回通用错误提示，不暴露技术细节
     * - 保护系统安全性，避免信息泄露
     * 
     * 常见异常类型：
     * - IOException: 输入输出异常
     * - SQLException: 数据库异常
     * - NetworkException: 网络连接异常
     * - 其他系统级异常
     * 
     * @param e 通用异常对象
     * @return 包含通用错误信息的API响应，状态码500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        // 在服务端记录完整错误信息，便于问题排查和系统监控
        LoggerUtil.error("系统异常", e);
        
        // 向客户端返回通用错误信息，不暴露系统内部实现细节
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(500, "系统繁忙，请稍后重试"));
    }

    /**
     * 安全错误消息过滤方法
     * 
     * 检查和过滤错误消息中可能包含的敏感信息，防止系统内部信息泄露。
     * 识别常见的技术关键词并将其替换为用户友好的通用提示。
     * 
     * 过滤规则：
     * - 数据库相关：包含"database"或"SQL"的消息转换为"数据处理失败"
     * - 可扩展：可根据需要添加更多敏感关键词的检测
     * - 默认保留：不包含敏感信息的消息原样返回
     * 
     * 安全考虑：
     * - 防止SQL注入信息泄露
     * - 隐藏数据库结构信息
     * - 保护系统架构细节
     * - 提供用户友好的错误提示
     * 
     * @param originalMessage 原始错误消息
     * @return 安全过滤后的错误消息
     */
    private String getSafeErrorMessage(String originalMessage) {
        // 检查是否包含数据库相关的敏感信息
        if (originalMessage != null && 
            (originalMessage.toLowerCase().contains("database") || 
             originalMessage.toLowerCase().contains("sql"))) {
            return "数据处理失败";
        }
        
        // 过滤其他敏感信息
        if (originalMessage != null) {
            String lowerMessage = originalMessage.toLowerCase();
            if (lowerMessage.contains("password") || 
                lowerMessage.contains("token") ||
                lowerMessage.contains("secret") ||
                lowerMessage.contains("key") ||
                lowerMessage.contains("file not found") ||
                lowerMessage.contains("access denied") ||
                lowerMessage.contains("connection") ||
                lowerMessage.contains("timeout")) {
                return "操作失败，请稍后重试";
            }
        }
        
        // 不包含敏感信息的消息原样返回
        return originalMessage;
    }
}