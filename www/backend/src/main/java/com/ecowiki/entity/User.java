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

/**
 * 用户实体类
 * 
 * 代表系统中的用户信息，包含用户的基本信息和认证相关数据。
 * 该实体类已经过重构，移除了原有的userGroup字段，改用独立的user_roles表
 * 来管理用户与角色的多对多关系。
 * 
 * 数据库表：user
 * 
 * 主要字段：
 * - userId: 用户唯一标识
 * - username: 用户名（唯一）
 * - email: 邮箱地址（唯一，用于登录）
 * - password: 加密后的密码
 * - fullName: 用户全名
 * - active: 账户是否激活
 * - gender: 性别（0=未设置，1=男，2=女）
 * - emailVerified: 邮箱验证状态
 * - loginToken: 登录令牌
 * - roleId: 角色ID（与user_roles表配合使用）
 * 
 * 关系：
 * - 通过user_roles表与Role实体建立多对多关系
 * 
 * @author EcoWiki Team
 * @version 2.0 (重构后，移除userGroup字段)
 * @since 2025-06-30
 */
@Entity
@Table(name = "user")
public class User {
    
    /**
     * 用户ID - 主键，自动递增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 用户名 - 唯一，非空
     * 用于用户登录和显示
     */
    @Column(unique = true, nullable = false)
    private String username;
    
    /**
     * 密码 - 非空
     * 存储加密后的密码哈希值
     */
    @Column(nullable = false)
    private String password;
    
    /**
     * 邮箱地址 - 唯一，非空
     * 可用于登录和找回密码
     */
    @Column(unique = true, nullable = false)
    private String email;
    
    /**
     * 用户全名 - 可选
     * 用于显示用户的真实姓名
     */
    private String fullName;
    
    /**
     * 账户激活状态 - 非空，默认为true
     * false表示账户被禁用
     */
    @Column(nullable = false)
    private Boolean active = true;
    
    /**
     * 性别 - 可选
     * 0: 不设置/保密
     * 1: 男
     * 2: 女
     */
    @Column
    private Byte gender;
    
    /**
     * 邮箱验证状态 - 可选
     * true: 已验证
     * false: 未验证
     * null: 未设置
     */
    @Column
    private Boolean emailVerified;
    
    /**
     * 登录令牌 - 可选
     * 用于记住登录状态或单点登录
     */
    @Column
    private String loginToken;
    
    /**
     * 角色ID - 可选
     * 与user_roles表配合使用，支持多角色分配
     */
    @Column
    private Integer roleId;
    
    @Column
    private String permissions; // 权限描述
    
    @Column
    private LocalDateTime lastLogin; // 最后登录时间
    
    @Column
    private String avatarUrl; // 头像地址

    @Column(name = "security_question")
    private String securityQuestion; // 安全问题
    
    @Column(name = "security_answer")
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