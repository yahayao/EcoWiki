/**
 * 统一API响应格式类
 * 
 * 功能包括：
 * - 封装RESTful API统一响应结构
 * - 提供成功和失败响应的工厂方法
 * - 支持泛型数据类型封装
 * - 包含状态码、消息和时间戳信息
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.dto;

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