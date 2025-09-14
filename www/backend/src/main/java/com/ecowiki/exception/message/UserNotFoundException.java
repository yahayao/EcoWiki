/**
 * 用户未找到异常
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.exception.message;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}