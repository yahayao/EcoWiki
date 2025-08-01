package com.ecowiki.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 密码修改数据传输对象
 * <p>
 * 用于用户修改密码时的数据传输和验证。
 * 包含原密码验证和新密码设置功能。
 * <p>
 * <b>安全设计：</b>
 * - 要求提供原密码进行身份验证
 * - 新密码强度验证（长度、复杂度）
 * - 确认密码一致性检查
 * - 防止密码重复使用
 * 
 * @author EcoWiki Development Team
 * @version 1.0
 * @since 2025-08-01
 */
public class PasswordChangeDto {
    
    /**
     * 当前密码（用于身份验证）
     */
    @NotBlank(message = "当前密码不能为空")
    private String currentPassword;
    
    /**
     * 新密码
     * 要求：8-32位，包含字母、数字
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度必须在8-32位之间")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,32}$",
        message = "密码必须包含字母和数字，可包含特殊字符@$!%*?&"
    )
    private String newPassword;
    
    /**
     * 确认新密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    // ================ 构造方法 ================
    
    /**
     * 默认构造方法
     */
    public PasswordChangeDto() {}
    
    /**
     * 完整构造方法
     * 
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     */
    public PasswordChangeDto(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
    
    // ================ Getter/Setter 方法 ================
    
    /**
     * 获取当前密码
     * 
     * @return 当前密码
     */
    public String getCurrentPassword() {
        return currentPassword;
    }
    
    /**
     * 设置当前密码
     * 
     * @param currentPassword 当前密码
     */
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
    
    /**
     * 获取新密码
     * 
     * @return 新密码
     */
    public String getNewPassword() {
        return newPassword;
    }
    
    /**
     * 设置新密码
     * 
     * @param newPassword 新密码
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    /**
     * 获取确认密码
     * 
     * @return 确认密码
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    /**
     * 设置确认密码
     * 
     * @param confirmPassword 确认密码
     */
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    // ================ 业务方法 ================
    
    /**
     * 验证新密码和确认密码是否一致
     * 
     * @return true表示密码一致，false表示不一致
     */
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
    
    /**
     * 验证新密码是否与当前密码相同
     * 
     * @return true表示密码相同，false表示不同
     */
    public boolean isSameAsCurrentPassword() {
        return currentPassword != null && currentPassword.equals(newPassword);
    }
    
    /**
     * 验证数据完整性
     * 
     * @return true表示数据完整且有效，false表示数据无效
     */
    public boolean isValid() {
        return currentPassword != null && !currentPassword.trim().isEmpty() &&
               newPassword != null && !newPassword.trim().isEmpty() &&
               confirmPassword != null && !confirmPassword.trim().isEmpty() &&
               isPasswordMatch() && !isSameAsCurrentPassword();
    }
    
    // ================ 重写方法 ================
    
    /**
     * 重写toString方法（隐藏敏感信息）
     */
    @Override
    public String toString() {
        return "PasswordChangeDto{" +
                "currentPassword='[HIDDEN]'" +
                ", newPassword='[HIDDEN]'" +
                ", confirmPassword='[HIDDEN]'" +
                '}';
    }
    
    /**
     * 重写equals方法
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PasswordChangeDto that = (PasswordChangeDto) obj;
        return java.util.Objects.equals(currentPassword, that.currentPassword) &&
               java.util.Objects.equals(newPassword, that.newPassword) &&
               java.util.Objects.equals(confirmPassword, that.confirmPassword);
    }
    
    /**
     * 重写hashCode方法
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(currentPassword, newPassword, confirmPassword);
    }
}
