/**
 * 权限数据传输对象DTO类
 * 
 * 功能包括：
 * - 权限信息数据封装
 * - 权限管理界面数据传输
 * - 权限实体轻量级表示
 * - API数据交换支持
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.dto.permission;

import java.time.LocalDateTime;

public class PermissionDto {
    
    /**
     * 权限唯一标识符
     * 数据库中的主键，用于唯一标识权限
     */
    private Integer permissionId;
    
    /**
     * 权限名称/标识符
     * 权限的代码标识，用于程序中的权限检查
     * 采用"资源:操作"格式，如"user:read", "admin:write"
     */
    private String permissionName;
    
    /**
     * 权限描述
     * 权限功能的人性化描述，用于界面显示
     * 帮助管理员理解权限的具体作用
     */
    private String description;
    
    /**
     * 权限创建时间
     * 记录权限在系统中的创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 权限更新时间
     * 记录权限信息的最后更新时间
     */
    private LocalDateTime updatedAt;

    // ==================== 构造函数 ====================
    
    /**
     * 默认构造函数
     * 用于框架反序列化和空对象创建
     */
    public PermissionDto() {}

    /**
     * 便捷构造函数
     * 用于快速创建包含核心信息的权限DTO对象
     * 
     * @param permissionId 权限ID
     * @param permissionName 权限名称
     * @param description 权限描述
     */
    public PermissionDto(Integer permissionId, String permissionName, String description) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
        this.description = description;
    }

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
     * @param permissionName 权限标识符
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