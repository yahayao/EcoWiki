/**
 * 权限实体类
 * 
 * 功能包括：
 * - 系统权限定义和管理
 * - 权限名称和资源标识
 * - 权限分组和层级管理
 * - 角色权限关联支持
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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "permission")
public class Permission {
    
    /**
     * 权限唯一标识符
     * 数据库主键，自动递增
     * 用于唯一标识每个权限记录
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Integer permissionId;

    /**
     * 权限名称/标识符
     * 权限的唯一标识符，用于代码中的权限检查
     * 采用"资源:操作"的命名格式，如"user:read", "admin:write"
     * 必须唯一且不能为空
     */
    @Column(name = "permission_name", nullable = false)
    private String permissionName;

    /**
     * 权限描述
     * 权限功能的人性化描述，用于界面显示和说明
     * 帮助管理员理解权限的具体作用和适用场景
     */
    @Column(name = "description")
    private String description;

    /**
     * 权限创建时间
     * 记录权限定义的创建时间
     * 设置为不可更新，确保审计数据的准确性
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 权限最后更新时间
     * 记录权限信息的最后修改时间
     * 用于追踪权限定义的变更历史
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== Getter和Setter方法 ====================
    
    /**
     * 获取权限ID
     * @return 权限唯一标识符
     */
    public Integer getPermissionId() { 
        return permissionId; 
    }
    
    /**
     * 设置权限ID
     * @param permissionId 权限唯一标识符
     */
    public void setPermissionId(Integer permissionId) { 
        this.permissionId = permissionId; 
    }

    /**
     * 获取权限名称
     * @return 权限标识符字符串
     */
    public String getPermissionName() { 
        return permissionName; 
    }
    
    /**
     * 设置权限名称
     * @param permissionName 权限标识符，不能为空
     */
    public void setPermissionName(String permissionName) { 
        this.permissionName = permissionName; 
    }

    /**
     * 获取权限描述
     * @return 权限功能描述
     */
    public String getDescription() { 
        return description; 
    }
    
    /**
     * 设置权限描述
     * @param description 权限功能描述
     */
    public void setDescription(String description) { 
        this.description = description; 
    }

    /**
     * 获取创建时间
     * @return 权限创建时间
     */
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    /**
     * 设置创建时间
     * @param createdAt 权限创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }

    /**
     * 获取更新时间
     * @return 权限最后更新时间
     */
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    
    /**
     * 设置更新时间
     * @param updatedAt 权限最后更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = updatedAt; 
    }
}