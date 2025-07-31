package com.ecowiki.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统一日志工具类
 * 
 * 提供统一的日志记录方法，替换项目中的System.out.println和System.err.println。
 * 基于SLF4J接口，使用Logback作为实际的日志实现。
 * 
 * 主要功能：
 * - 提供不同级别的日志记录方法（DEBUG、INFO、WARN、ERROR）
 * - 支持参数化日志消息，提高性能
 * - 统一的异常日志记录
 * - 便于日志级别控制和输出格式管理
 * 
 * 使用示例：
 * <pre>
 * LoggerUtil.info("用户登录成功，用户名: {}", username);
 * LoggerUtil.error("文件处理失败", exception);
 * LoggerUtil.debug("处理请求: {}, 参数: {}", requestPath, params);
 * </pre>
 * 
 * @author EcoWiki团队
 * @version 1.0
 * @since 2024-01-01
 */
public class LoggerUtil {
    
    /**
     * 获取调用者的Logger实例
     * 
     * 通过堆栈跟踪获取实际调用LoggerUtil的类，
     * 为该类创建专用的Logger实例，便于日志分类。
     * 
     * @return Logger实例
     */
    private static Logger getLogger() {
        // 获取调用堆栈，找到真正的调用者
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        // stackTrace[0] = Thread.getStackTrace()
        // stackTrace[1] = LoggerUtil.getLogger()
        // stackTrace[2] = LoggerUtil的public方法
        // stackTrace[3] = 真正的调用者
        if (stackTrace.length > 3) {
            String callerClassName = stackTrace[3].getClassName();
            return LoggerFactory.getLogger(callerClassName);
        }
        
        // 备用方案：使用LoggerUtil作为logger名称
        return LoggerFactory.getLogger(LoggerUtil.class);
    }
    
    /**
     * 记录DEBUG级别日志
     * 
     * @param message 日志消息
     */
    public static void debug(String message) {
        getLogger().debug(message);
    }
    
    /**
     * 记录DEBUG级别日志（带参数）
     * 
     * @param message 日志消息模板
     * @param args 参数
     */
    public static void debug(String message, Object... args) {
        getLogger().debug(message, args);
    }
    
    /**
     * 记录INFO级别日志
     * 
     * @param message 日志消息
     */
    public static void info(String message) {
        getLogger().info(message);
    }
    
    /**
     * 记录INFO级别日志（带参数）
     * 
     * @param message 日志消息模板
     * @param args 参数
     */
    public static void info(String message, Object... args) {
        getLogger().info(message, args);
    }
    
    /**
     * 记录WARN级别日志
     * 
     * @param message 日志消息
     */
    public static void warn(String message) {
        getLogger().warn(message);
    }
    
    /**
     * 记录WARN级别日志（带参数）
     * 
     * @param message 日志消息模板
     * @param args 参数
     */
    public static void warn(String message, Object... args) {
        getLogger().warn(message, args);
    }
    
    /**
     * 记录WARN级别日志（带异常）
     * 
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void warn(String message, Throwable throwable) {
        getLogger().warn(message, throwable);
    }
    
    /**
     * 记录ERROR级别日志
     * 
     * @param message 日志消息
     */
    public static void error(String message) {
        getLogger().error(message);
    }
    
    /**
     * 记录ERROR级别日志（带参数）
     * 
     * @param message 日志消息模板
     * @param args 参数
     */
    public static void error(String message, Object... args) {
        getLogger().error(message, args);
    }
    
    /**
     * 记录ERROR级别日志（带异常）
     * 
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void error(String message, Throwable throwable) {
        getLogger().error(message, throwable);
    }
    
    /**
     * 记录异常信息的便捷方法
     * 
     * @param operation 操作描述
     * @param throwable 异常对象
     */
    public static void logException(String operation, Throwable throwable) {
        getLogger().error("{}失败: {}", operation, throwable.getMessage(), throwable);
    }
    
    /**
     * 记录业务操作日志
     * 
     * @param operation 操作名称
     * @param details 操作详情
     */
    public static void logOperation(String operation, String details) {
        getLogger().info("业务操作 - {}: {}", operation, details);
    }
    
    /**
     * 记录性能监控日志
     * 
     * @param operation 操作名称
     * @param duration 耗时（毫秒）
     */
    public static void logPerformance(String operation, long duration) {
        if (duration > 1000) {
            getLogger().warn("性能警告 - {} 耗时: {}ms", operation, duration);
        } else {
            getLogger().debug("性能监控 - {} 耗时: {}ms", operation, duration);
        }
    }
}
