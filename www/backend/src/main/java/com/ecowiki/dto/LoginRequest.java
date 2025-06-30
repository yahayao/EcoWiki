package com.ecowiki.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户登录请求DTO
 * <p>
 * 用于接收前端用户登录的请求数据，支持用户名或邮箱登录。
 * 支持数据验证和"记住我"功能。
 * <p>
 * <b>设计说明：</b>
 * - 支持用户名或邮箱两种登录方式
 * - 包含密码验证规则
 * - 支持"记住我"功能（可生成长期有效token）
 * - 使用Bean Validation进行数据验证
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
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