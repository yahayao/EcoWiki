package com.ecowiki.entity.user;

import java.io.Serializable;
import java.util.Objects;

/**
 * 用户角色复合主键类
 * 
 * 用于UserRole实体的复合主键定义。JPA要求复合主键类必须实现Serializable接口，
 * 并且必须重写equals()和hashCode()方法。
 * 
 * 设计原理：
 * - 确保用户和角色的组合唯一性
 * - 支持JPA的复合主键机制
 * - 提供高效的查找和比较操作
 * 
 * 主要字段：
 * - userId: 用户ID
 * - roleId: 角色ID
 * 
 * 使用场景：
 * - UserRole实体的@IdClass注解
 * - JPA查询中的复合主键条件
 * - 缓存和集合操作中的键值
 * 
 * 注意事项：
 * - 必须实现Serializable接口
 * - 必须有无参构造函数
 * - 必须重写equals()和hashCode()方法
 * - 字段名必须与UserRole实体中的主键字段名一致
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-06-30
 */
public class UserRoleId implements Serializable {
    
    /**
     * 序列化版本号
     * 用于序列化兼容性检查
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     * 必须与UserRole实体中的userId字段名称一致
     */
    private Integer userId;
    
    /**
     * 角色ID
     * 必须与UserRole实体中的roleId字段名称一致
     */
    private Integer roleId;

    /**
     * 默认构造函数
     * JPA要求复合主键类必须有无参构造函数
     */
    public UserRoleId() {}

    /**
     * 带参构造函数
     * 用于创建新的复合主键实例
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    public UserRoleId(Integer userId, Integer roleId) {
        this.userId = userId;
        this.roleId = roleId;
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
     * 判断两个复合主键是否相等
     * 
     * 用于JPA实体的比较和缓存操作。两个UserRoleId对象相等当且仅当
     * 它们的userId和roleId都相等。
     * 
     * @param o 要比较的对象
     * @return 如果相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleId that = (UserRoleId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
    }

    /**
     * 计算复合主键的哈希码
     * 
     * 用于HashMap、HashSet等集合操作。哈希码基于userId和roleId计算，
     * 确保相等的对象具有相同的哈希码。
     * 
     * @return 哈希码值
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
    
    /**
     * 返回复合主键的字符串表示
     * 
     * @return 格式化的字符串，包含userId和roleId
     */
    @Override
    public String toString() {
        return "UserRoleId{" +
                "userId=" + userId +
                ", roleId=" + roleId +
                '}';
    }
}
