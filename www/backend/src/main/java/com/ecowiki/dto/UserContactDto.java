package com.ecowiki.dto;

/**
 * 用户联系人数据传输对象
 * 
 * 用于消息发送时的联系人选择功能，包含用户的基本信息。
 * 相比完整的User实体，这个DTO只包含联系人选择所需的必要字段。
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-07-16
 */
public class UserContactDto {
    /** 用户ID */
    private Long userId;
    
    /** 用户名 */
    private String username;
    
    /** 邮箱地址 */
    private String email;
    
    /** 用户全名 */
    private String fullName;
    
    /** 账户是否激活 */
    private Boolean active;
    
    /** 角色ID */
    private Integer roleId;
    
    /** 角色名称 */
    private String roleName;
    
    /** 头像URL（可选） */
    private String avatarUrl;
    
    // 构造函数
    public UserContactDto() {
    }
    
    public UserContactDto(Long userId, String username, String email, String fullName, Boolean active) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.active = active;
    }
    
    // Getter和Setter方法
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Integer getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    @Override
    public String toString() {
        return "UserContactDto{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", active=" + active +
                ", roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                '}';
    }
}
