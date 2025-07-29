package com.ecowiki.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 密码重置请求数据传输对象
 * 
 * 该DTO用于处理用户重置密码时的完整请求数据，包含邮箱、验证答案和新密码。
 * 作为密码重置流程的最终步骤，确保用户能够安全地设置新密码。
 *
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
public class ResetPasswordRequest {
    /** 用户名（可选，与邮箱二选一） */
    private String username;
    /** 邮箱（可选，与用户名二选一） */
    private String email;
    
    /** 验证答案 */
    @NotBlank(message = "验证答案不能为空")
    private String securityAnswer;
    
    /** 用户设置的新密码 */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "新密码长度至少为6位")
    private String newPassword;
    
    public ResetPasswordRequest() {}

    // ==================== Getter和Setter方法 ====================
    
    /**
     * 获取邮箱地址
     * @return 用户邮箱地址
     */
    public String getEmail() { 
        return email; 
    }
    
    /**
     * 设置邮箱地址
     * @param email 用户邮箱地址
     */
    public void setEmail(String email) { 
        this.email = email; 
    }

    /**
     * 获取验证答案
     * @return 安全问题答案或验证码
     */
    public String getSecurityAnswer() { 
        return securityAnswer; 
    }
    
    /**
     * 设置验证答案
     * @param securityAnswer 安全问题答案或验证码
     */
    public void setSecurityAnswer(String securityAnswer) { 
        this.securityAnswer = securityAnswer; 
    }

    /**
     * 获取新密码
     * @return 用户设置的新密码
     */
    public String getNewPassword() { 
        return newPassword; 
    }
    
    /**
     * 设置新密码
     * @param newPassword 用户设置的新密码，必须符合密码强度要求
     */
    public void setNewPassword(String newPassword) { 
        this.newPassword = newPassword; 
    }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
