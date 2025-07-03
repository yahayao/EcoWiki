package com.ecowiki.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * 角色权限关联实体类
 * 
 * 该实体类映射数据库中的role_permissions表，用于管理角色与权限之间的多对多关系。
 * 采用复合主键设计，通过角色ID和权限ID的组合确保关联关系的唯一性。
 * 
 * 主要功能：
 * - 角色权限关联：建立角色与权限的多对多关系映射
 * - 复合主键设计：使用角色ID和权限ID作为联合主键
 * - 关联时间记录：记录权限分配的创建时间
 * - 数据完整性：确保角色权限关联的一致性和可追溯性
 * 
 * 数据库表结构：
 * - role_id: 角色ID（外键，指向roles表）
 * - permission_id: 权限ID（外键，指向permissions表）
 * - created_at: 权限分配创建时间
 * 
 * 设计特点：
 * - 多对多关系：一个角色可以拥有多个权限，一个权限可以分配给多个角色
 * - 复合主键：使用@IdClass注解配置复合主键类
 * - 时间戳记录：记录权限分配的时间，便于审计和追踪
 * - 外键约束：确保引用的角色和权限在对应表中存在
 * 
 * 使用场景：
 * - 权限管理系统：动态分配和管理角色权限
 * - 访问控制：基于角色的访问控制(RBAC)实现
 * - 权限审计：追踪权限变更历史和分配记录
 * - 批量权限操作：支持批量分配和撤销权限
 * 
 * 技术实现：
 * - JPA复合主键：使用@IdClass配置复合主键映射
 * - 时间戳自动管理：创建时间不可更新(updatable = false)
 * - 数据库约束：通过nullable = false确保关键字段非空
 * - 实体关联：通过外键维护数据一致性
 * 
 * 注意事项：
 * - 复合主键类RolePermissionId必须实现Serializable接口
 * - 创建时间字段设置为不可更新，确保审计数据准确性
 * - 删除角色或权限时需要级联删除相关的关联记录
 * - 批量操作时注意事务管理，确保数据一致性
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 * @see RolePermissionId 复合主键类
 * @see Role 角色实体类
 * @see Permission 权限实体类
 */
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