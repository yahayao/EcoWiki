package com.ecowiki.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String fullName;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column
    private Byte gender; // 0为不设置，1为男，2为女
    
    @Column
    private Boolean emailVerified; // 邮箱验证状态
    
    @Column
    private String loginToken; // 登录令牌
    
    @Column
    private Integer roleId; // 角色ID
    
    @Column
    private String permissions; // 权限描述
    
    @Column
    private LocalDateTime lastLogin; // 最后登录时间
    
    @Column
    private String avatarUrl; // 头像地址
    
    @Column
    private String bio; // 个人简介

    @Column
    private String securityQuestion; // 安全问题
    
    @Column
    private String securityAnswer; // 问题答案
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 构造函数
    public User() {}
    
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.active = true;
        this.emailVerified = false;
        this.gender = 0;
    }
    
    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public Byte getGender() { return gender; }
    public void setGender(Byte gender) { this.gender = gender; }
    
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    
    public String getLoginToken() { return loginToken; }
    public void setLoginToken(String loginToken) { this.loginToken = loginToken; }
    
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }
    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }
    
    // 临时权限检查方法 - 这些方法现在需要通过UserService来实现
    // 为了保持向后兼容性，暂时返回false，实际使用时应该通过UserService检查
    public boolean isAdmin() {
        // TODO: 这个方法现在应该通过UserService.getUserRoleName()来实现
        return false;
    }
    
    public boolean isSuperAdmin() {
        // TODO: 这个方法现在应该通过UserService.getUserRoleName()来实现
        return false;
    }
    
    public boolean hasPermission(String requiredGroup) {
        // TODO: 这个方法现在应该通过UserService来实现权限检查
        return false;
    }
    
    // 临时兼容性方法 - 角色信息现在存储在User_Roles表中
    public String getUserGroup() {
        // TODO: 这个方法现在应该通过UserService.getUserRoleName()来实现
        return "user"; // 返回默认值以保持兼容性
    }
    
    public void setUserGroup(String userGroup) {
        // TODO: 这个方法现在应该通过UserService来实现角色设置
        // 暂时不做任何操作，保持兼容性
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
        if (this.emailVerified == null) {
            this.emailVerified = false;
        }
        if (this.gender == null) {
            this.gender = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}