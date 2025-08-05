/**
 * 用户登录请求DTO类
 * 
 * 功能包括：
 * - 用户登录信息数据封装
 * - 用户名或邮箱登录支持
 * - 登录数据验证和校验
 * - "记住我"功能支持
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    /** 用户名（可选，与邮箱二选一） */
    private String username;
    /** 邮箱（可选，与用户名二选一） */
    private String email;
    
    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少为6位")
    private String password;
    
    /** 是否记住我 */
    private boolean rememberMe;

    // Constructors
    public LoginRequest() {}

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }
}
