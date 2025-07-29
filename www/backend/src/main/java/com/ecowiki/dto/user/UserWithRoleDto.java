package com.ecowiki.dto.user;

import java.time.LocalDateTime;

/**
 * 用户角色DTO
 * <p>
 * 用于前后端传输的用户信息数据传输对象，包含用户基本信息和关联的角色名称。
 * 主要用于管理后台的用户列表展示、角色分配等场景。
 * <p>
 * <b>设计说明：</b>
 * - 避免暴露敏感信息，不包含密码等字段
 * - 包含时间戳便于审计和排序
 * - 支持前端用户管理界面的数据绑定
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
public class UserWithRoleDto {
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 邮箱地址 */
    private String email;
    /** 账户是否激活 */
    private Boolean active;
    /** 账户创建时间 */
    private LocalDateTime createdAt;
    /** 最后更新时间 */
    private LocalDateTime updatedAt;
    /** 角色名称 */
    private String roleName;
    
    public UserWithRoleDto() {}
    
    public UserWithRoleDto(Long userId, String username, String email, Boolean active, 
                          LocalDateTime createdAt, LocalDateTime updatedAt, String roleName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roleName = roleName;
    }
    
    // Getters and Setters
    public Long getUserId() {  // 改为getUserId
        return userId;
    }
    
    public void setUserId(Long userId) {  // 改为setUserId
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
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    // 为了保持兼容性，提供getId方法
    public Long getId() {
        return this.userId;
    }
    
    public void setId(Long id) {
        this.userId = id;
    }
    
    // 为了保持兼容性，提供getUserGroup方法
    public String getUserGroup() {
        return this.roleName;
    }
    
    public void setUserGroup(String userGroup) {
        this.roleName = userGroup;
    }
}
