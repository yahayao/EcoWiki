/**
 * 用户角色关联实体类
 * 
 * 功能包括：
 * - 用户与角色的多对多关系管理
 * - 角色分配和权限授予
 * - 用户权限体系支持
 * - 角色分配时间记录
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-06-28
 * @lastModified 2025-08-05
 */
package com.ecowiki.entity.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

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