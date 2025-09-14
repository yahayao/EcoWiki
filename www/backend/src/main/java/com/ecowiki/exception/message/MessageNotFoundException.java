/**
 * 消息未找到异常
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.exception.message;

public class MessageNotFoundException extends RuntimeException {
    
    public MessageNotFoundException(String message) {
        super(message);
    }
    
    public MessageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}