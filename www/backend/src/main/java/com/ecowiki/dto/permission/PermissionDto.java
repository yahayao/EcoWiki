package com.ecowiki.dto.permission;

import com.ecowiki.entity.user.Permission;

import java.time.LocalDateTime;

/**
 * 权限数据传输对象
 * 
 * 该DTO用于权限信息在不同层之间的数据传输，提供权限实体的简化表示。
 * 主要用于权限管理界面的数据展示和权限相关API的数据交换。
 * 
 * 主要用途：
 * - 权限信息传输：在控制器、服务层和前端之间传输权限数据
 * - 数据简化：提供权限实体的轻量级表示
 * - API响应：作为权限查询和管理接口的响应数据
 * - 前端展示：为权限管理界面提供标准化的数据格式
 * 
 * 功能特性：
 * - 完整权限信息：包含权限的所有核心属性
 * - 时间戳记录：包含创建和更新时间信息
 * - 灵活构造：提供多种构造方式适应不同场景
 * - 数据映射：与权限实体类保持结构一致
 * 
 * 使用场景：
 * - 权限列表查询：获取系统中所有权限列表
 * - 权限详情展示：显示特定权限的详细信息
 * - 权限分配界面：为角色分配权限时的选项列表
 * - 权限检查结果：返回用户或角色的权限信息
 * - 管理后台：权限管理功能的数据载体
 * 
 * 数据结构：
 * - 权限标识：唯一的权限ID和权限名称
 * - 描述信息：人性化的权限功能描述
 * - 时间信息：创建时间和最后更新时间
 * - 元数据：权限的其他扩展信息
 * 
 * 设计优势：
 * - 数据隔离：避免直接暴露实体类的内部结构
 * - 传输优化：减少不必要的数据传输量
 * - 版本兼容：便于API版本升级和兼容性管理
 * - 安全性：控制对外暴露的数据字段
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 * @see Permission 权限实体类
 */
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