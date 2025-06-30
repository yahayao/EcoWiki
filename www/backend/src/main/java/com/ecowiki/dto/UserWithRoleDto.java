package com.ecowiki.dto;

import java.time.LocalDateTime;

public class UserWithRoleDto {
    private Long userId;  // 改为userId，与前端保持一致
    private String username;
    private String email;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
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
