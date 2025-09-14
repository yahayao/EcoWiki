/**
 * 消息访问被拒绝异常
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.exception.message;

public class MessageAccessDeniedException extends RuntimeException {
    
    public MessageAccessDeniedException(String message) {
        super(message);
    }
    
    public MessageAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}