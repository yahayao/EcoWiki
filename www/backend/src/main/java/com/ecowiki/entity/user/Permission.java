package com.ecowiki.entity.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 权限实体类
 * 
 * 该实体类映射数据库中的permission表，用于管理系统的权限信息。
 * 权限是访问控制系统的核心组件，定义了用户可以执行的操作和访问的资源。
 * 
 * 主要功能：
 * - 权限定义：存储系统中所有可用的权限信息
 * - 权限标识：通过唯一的权限名称标识不同的操作权限
 * - 权限描述：提供人性化的权限功能说明
 * - 时间追踪：记录权限的创建和更新时间
 * 
 * 数据库表结构：
 * - permission_id: 权限唯一标识符（主键，自增）
 * - permission_name: 权限名称/标识符（唯一，如'user:read', 'admin:write'）
 * - description: 权限功能描述
 * - created_at: 权限创建时间
 * - updated_at: 权限最后更新时间
 * 
 * 权限命名规范：
 * - 资源:操作 格式，如：user:read, article:write, system:admin
 * - 分层权限：支持细粒度的权限控制
 * - 通配符权限：支持*通配符表示全部权限
 * 
 * 设计特点：
 * - 细粒度控制：支持精确到具体操作的权限定义
 * - 可扩展性：便于添加新的权限类型和操作
 * - 层次化管理：支持权限的分类和层次结构
 * - 审计支持：完整的时间戳记录权限变更历史
 * 
 * 使用场景：
 * - 基于角色的访问控制(RBAC)：与角色关联实现权限管理
 * - API接口保护：控制对特定接口的访问权限
 * - 功能模块控制：控制用户对不同功能模块的访问
 * - 数据权限管理：控制用户对特定数据的访问权限
 * 
 * 权限示例：
 * - system:admin - 系统管理权限
 * - user:read - 用户信息查看权限
 * - user:write - 用户信息修改权限
 * - article:publish - 文章发布权限
 * - comment:moderate - 评论审核权限
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 * @see Role 角色实体类
 * @see RolePermission 角色权限关联类
 */
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