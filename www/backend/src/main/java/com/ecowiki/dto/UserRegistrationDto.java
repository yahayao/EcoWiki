package com.ecowiki.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求DTO
 * <p>
 * 用于接收前端用户注册的请求数据，包含用户的基本信息和验证规则。
 * 支持完整的数据验证，确保注册信息的完整性和有效性。
 * <p>
 * <b>设计说明：</b>
 * - 包含用户注册所需的核心字段
 * - 使用Bean Validation进行全面的数据验证
 * - 支持邮箱格式验证和长度限制
 * - 适用于用户注册、信息收集等场景
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
public class UserRegistrationDto {
    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;
    
    /** 邮箱地址 */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    /** 全名（可选） */
    @Size(max = 100, message = "全名长度不能超过100个字符")
    private String fullName;

    // Constructors
    public UserRegistrationDto() {}

    public UserRegistrationDto(String username, String password, String email, String fullName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
