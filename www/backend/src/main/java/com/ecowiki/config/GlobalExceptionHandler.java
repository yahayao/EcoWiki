package com.ecowiki.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ecowiki.dto.ApiResponse;

/**
 * 全局异常处理器
 * 
 * 该类使用@RestControllerAdvice注解，提供全局统一的异常处理机制。
 * 捕获应用程序中抛出的各种异常，并将其转换为标准化的API响应格式。
 * 
 * 主要功能：
 * - 全局异常捕获：统一处理应用程序中的所有异常
 * - 安全信息过滤：防止敏感系统信息泄露给客户端
 * - 标准化响应：将异常转换为统一的API响应格式
 * - 错误日志记录：记录系统错误信息便于问题排查
 * - 用户友好提示：提供清晰的错误信息给前端用户
 * 
 * 异常处理策略：
 * - RuntimeException：业务逻辑异常，返回400状态码
 * - Exception：系统级异常，返回500状态码并隐藏详细信息
 * - 敏感信息过滤：过滤数据库、SQL等技术细节信息
 * - 错误信息安全化：将技术异常转换为用户友好的提示
 * 
 * 安全特性：
 * - 信息泄露防护：避免向客户端暴露系统内部信息
 * - 敏感关键词过滤：识别并过滤包含敏感信息的错误消息
 * - 通用错误响应：对于系统级错误返回通用提示信息
 * - 错误日志记录：在服务端记录完整错误信息用于排查
 * 
 * 响应格式：
 * - 统一使用ApiResponse格式
 * - 包含错误码、错误信息和时间戳
 * - 保持与正常业务响应的格式一致性
 * 
 * 使用场景：
 * - Web API异常处理：处理RESTful接口中的异常
 * - 业务逻辑异常：处理业务层抛出的业务异常
 * - 系统运行异常：处理系统运行时的技术异常
 * - 安全异常处理：处理认证、授权相关的异常
 * 
 * 扩展建议：
 * - 异常分类处理：根据异常类型进行更细粒度的处理
 * - 国际化支持：支持多语言的错误信息提示
 * - 监控集成：集成监控系统进行异常统计和告警
 * - 自定义异常：支持业务自定义异常的特殊处理
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 * @see ApiResponse 统一API响应格式
 * @see RestControllerAdvice Spring全局异常处理注解
 */
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
        System.err.println("系统错误: " + e.getMessage());
        
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
        
        // TODO: 可根据需要添加更多敏感信息过滤规则
        // 例如：文件路径、内存地址、系统配置等
        
        // 不包含敏感信息的消息原样返回
        return originalMessage;
    }
}