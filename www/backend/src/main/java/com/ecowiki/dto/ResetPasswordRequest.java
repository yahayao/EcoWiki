package com.ecowiki.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
/**
 * 密码重置请求数据传输对象
 * 
 * 该DTO用于处理用户重置密码时的完整请求数据，包含邮箱、验证答案和新密码。
 * 作为密码重置流程的最终步骤，确保用户能够安全地设置新密码。
 * 
 * 主要用途：
 * - 密码重置验证：通过多重验证确认用户身份
 * - 新密码设置：接收和验证用户设置的新密码
 * - 安全保护：防止未授权的密码重置操作
 * - 流程完整性：确保重置流程的完整性和安全性
 * 
 * 业务流程：
 * 1. 用户通过忘记密码邮件进入重置页面
 * 2. 用户输入邮箱确认身份
 * 3. 用户回答安全问题或输入验证码
 * 4. 用户设置新密码并确认
 * 5. 系统验证所有信息后更新密码
 * 
 * 数据验证：
 * - 邮箱验证：确保邮箱与重置请求一致
 * - 答案验证：验证安全问题答案或验证码
 * - 密码强度：确保新密码符合安全要求
 * - 重复性检查：防止设置与旧密码相同的新密码
 * 
 * 安全特性：
 * - 多重验证：邮箱+验证答案双重确认
 * - 密码加密：新密码将被安全加密存储
 * - 会话失效：重置后使所有现有会话失效
 * - 操作日志：记录密码重置操作日志
 * 
 * 使用场景：
 * - 完成密码重置流程
 * - 账户安全恢复
 * - 密码安全更新
 * - 用户自助密码管理
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 * @see ForgotPasswordRequest 忘记密码请求DTO
 */
public class ResetPasswordRequest {
    
    /** 用户名（可选，与邮箱二选一） */
    private String username;
    /** 邮箱（可选，与用户名二选一） */
    private String email;
    
    /** 验证答案 */
    @NotBlank(message = "验证答案不能为空")
    private String securityAnswer;
    /**
     * 用户设置的新密码
     * 
     * 该字段用于接收用户在密码重置流程中输入的新密码。
     * 必须满足密码强度要求，且长度至少为6位。
     * 
     * @see jakarta.validation.constraints.NotBlank
     * @see jakarta.validation.constraints.Size
     */
    
    /** 新密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少为6位")
    private String newPassword;

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
    
    // ==================== 其他方法 ==========
}