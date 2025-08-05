/**
 * 角色权限关联复合主键类
 * 
 * 功能：
 * - 作为RolePermission实体的复合主键
 * - 用于JPA的@IdClass注解配置
 * - 实现Serializable接口确保序列化能力
 * - 提供equals/hashCode方法支持对象比较
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.entity.user;

import java.io.Serializable;
import java.util.Objects;
public class RolePermissionId implements Serializable {
    /**
     * 角色ID
     * 作为复合主键的一部分，引用roles表的主键
     */
    private Integer roleId;
    
    /**
     * 权限ID
     * 作为复合主键的一部分，引用permissions表的主键
     */
    private Integer permissionId;
    
    /**
     * 无参构造函数
     * JPA规范要求实体类和复合主键类必须提供无参构造函数
     */
    public RolePermissionId() {}
    
    /**
     * 有参构造函数
     * 便于快速创建复合主键实例
     * 
     * @param roleId 角色ID
     * @param permissionId 权限ID
     */
    public RolePermissionId(Integer roleId, Integer permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }
    
    /**
     * 获取角色ID
     * 
     * @return 角色ID
     */
    public Integer getRoleId() {
        return roleId;
    }
    
    /**
     * 设置角色ID
     * 
     * @param roleId 角色ID
     */
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
    
    /**
     * 获取权限ID
     * 
     * @return 权限ID
     */
    public Integer getPermissionId() {
        return permissionId;
    }
    
    /**
     * 设置权限ID
     * 
     * @param permissionId 权限ID
     */
    public void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }
    
    /**
     * 判断两个复合主键对象是否相等
     * 基于角色ID和权限ID进行比较
     * 
     * @param o 要比较的对象
     * @return 如果两个对象的角色ID和权限ID都相同则返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePermissionId that)) return false;
        return Objects.equals(roleId, that.roleId) && Objects.equals(permissionId, that.permissionId);
    }
    
    /**
     * 计算复合主键对象的哈希码
     * 基于角色ID和权限ID计算哈希值
     * 
     * @return 基于角色ID和权限ID的哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionId);
    }
}
