/**
 * 角色权限关联实体类
 * 
 * 功能包括：
 * - 角色与权限的多对多关系管理
 * - 权限分配和授权控制
 * - 角色权限体系构建
 * - 权限分配时间追踪
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
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
@Table(name = "role_permissions")
@IdClass(RolePermissionId.class)  // 指定复合主键类
public class RolePermission {
    
    /**
     * 角色ID
     * 复合主键的组成部分，引用roles表的主键
     * 与permissionId组合形成唯一的角色权限关联标识
     */
    @Id
    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    /**
     * 权限ID
     * 复合主键的组成部分，引用permissions表的主键
     * 与roleId组合形成唯一的角色权限关联标识
     */
    @Id
    @Column(name = "permission_id", nullable = false)
    private Integer permissionId;

    /**
     * 权限分配创建时间
     * 记录该角色权限关联关系的创建时间
     * 设置为不可更新，确保审计数据的准确性
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ==================== Getter和Setter方法 ====================
    
    /**
     * 获取角色ID
     * @return 角色ID
     */
    public Integer getRoleId() { 
        return roleId; 
    }
    
    /**
     * 设置角色ID
     * @param roleId 角色ID，不能为空
     */
    public void setRoleId(Integer roleId) { 
        this.roleId = roleId; 
    }

    /**
     * 获取权限ID
     * @return 权限ID
     */
    public Integer getPermissionId() { 
        return permissionId; 
    }
    
    /**
     * 设置权限ID
     * @param permissionId 权限ID，不能为空
     */
    public void setPermissionId(Integer permissionId) { 
        this.permissionId = permissionId; 
    }

    /**
     * 获取创建时间
     * @return 权限分配的创建时间
     */
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    /**
     * 设置创建时间
     * @param createdAt 权限分配的创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
}