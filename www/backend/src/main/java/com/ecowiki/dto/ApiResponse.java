package com.ecowiki.dto;

/**
 * 统一API响应格式
 * <p>
 * 用于封装所有RESTful API的响应数据，提供统一的响应结构。
 * 包含状态码、消息、数据和时间戳等字段，便于前端统一处理。
 * <p>
 * <b>设计说明：</b>
 * - 泛型T支持任意类型的数据响应
 * - 提供静态工厂方法便于创建成功/失败响应
 * - 时间戳用于调试和日志追踪
 *
 * @param <T> 响应数据类型
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
public class ApiResponse<T> {
    /** HTTP状态码 */
    private int code;
    /** 响应消息 */
    private String message;
    /** 响应数据 */
    private T data;
    /** 响应时间戳 */
    private long timestamp;
    /** 是否成功 */
    private boolean success;

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        this.success = code >= 200 && code < 300;
    }

    /**
     * 创建成功响应（带数据）
     * @param data 响应数据
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /**
     * 创建成功响应（带数据和消息）
     * @param data 响应数据
     * @param message 成功消息
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 创建成功响应（仅消息）
     * @param message 成功消息
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200, message, null);
    }

    /**
     * 创建错误响应（指定状态码）
     * @param code 错误状态码
     * @param message 错误消息
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 创建错误响应（默认400状态码）
     * @param message 错误消息
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, message, null);
    }

    // Getters and Setters
    public int getCode() { return code; }
    public void setCode(int code) { 
        this.code = code;
        this.success = code >= 200 && code < 300;
    }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}