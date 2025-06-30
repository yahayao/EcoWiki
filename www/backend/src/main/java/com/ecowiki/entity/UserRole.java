package com.ecowiki.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * 用户角色关联实体类
 * 
 * 用于管理用户与角色之间的多对多关系。该表是用户权限系统的核心，
 * 支持一个用户拥有多个角色，以及一个角色分配给多个用户。
 * 
 * 数据库表：user_roles
 * 
 * 设计特点：
 * - 使用复合主键（user_id, role_id）确保唯一性
 * - 记录角色分配的时间，便于审计
 * - 支持动态角色分配和撤销
 * 
 * 主要字段：
 * - userId: 用户ID（复合主键之一）
 * - roleId: 角色ID（复合主键之一）
 * - createdAt: 角色分配时间
 * 
 * 使用场景：
 * - 权限检查时查询用户的所有角色
 * - 管理后台中分配/撤销用户角色
 * - 角色管理中查看角色的用户列表
 * 
 * @author EcoWiki Team
 * @version 2.0 (重构后新增，替代User表中的userGroup字段)
 * @since 2025-06-30
 */
@Entity
@Table(name = "user_roles")
@IdClass(UserRoleId.class)
public class UserRole {
    
    /**
     * 用户ID - 复合主键之一
     * 关联到user表的user_id字段
     */
    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /**
     * 角色ID - 复合主键之一
     * 关联到roles表的role_id字段
     */
    @Id
    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    /**
     * 角色分配时间 - 不可更新
     * 记录该用户被分配此角色的时间，用于审计
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 默认构造函数
     * JPA要求实体类必须有无参构造函数
     */
    public UserRole() {}
    
    /**
     * 带参构造函数
     * 用于创建新的用户角色关联，自动设置创建时间
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    public UserRole(Integer userId, Integer roleId) {
        this.userId = userId;
        this.roleId = roleId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    
    /**
     * 获取用户ID
     * @return 用户ID
     */
    public Integer getUserId() { return userId; }
    
    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public void setUserId(Integer userId) { this.userId = userId; }

    /**
     * 获取角色ID
     * @return 角色ID
     */
    public Integer getRoleId() { return roleId; }
    
    /**
     * 设置角色ID
     * @param roleId 角色ID
     */
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    /**
     * 获取创建时间
     * @return 角色分配时间
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    /**
     * 设置创建时间
     * @param createdAt 角色分配时间
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}