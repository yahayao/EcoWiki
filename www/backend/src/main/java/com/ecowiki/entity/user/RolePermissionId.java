package com.ecowiki.entity.user;

import java.io.Serializable;
import java.util.Objects;

/**
 * 角色权限关联复合主键类
 * 
 * 该类作为RolePermission实体的复合主键，用于JPA的@IdClass注解配置。
 * 实现了Serializable接口，确保复合主键的序列化能力。
 * 
 * 主要功能：
 * - 复合主键定义：为角色权限关联表定义联合主键
 * - 唯一性保证：通过角色ID和权限ID的组合确保记录唯一性
 * - equals/hashCode：实现对象比较和哈希计算
 * - 序列化支持：实现Serializable接口，支持对象序列化
 * 
 * 设计特点：
 * - 复合主键：由roleId和permissionId组成联合主键
 * - 不可变性：一旦创建，主键值不应被随意修改
 * - 对象比较：正确实现equals和hashCode方法
 * - 序列化兼容：实现Serializable接口，支持分布式场景
 * 
 * 使用场景：
 * - JPA复合主键：配合@IdClass注解使用
 * - 关联查询：作为查询条件定位特定的角色权限关联
 * - 缓存键值：作为缓存的键值进行数据存储
 * - 数据传输：在服务间传输复合主键信息
 * 
 * 技术实现：
 * - 无参构造函数：JPA规范要求提供无参构造函数
 * - 有参构造函数：便于快速创建复合主键实例
 * - getter/setter：提供字段访问方法
 * - equals/hashCode：确保对象比较和集合操作的正确性
 * 
 * 注意事项：
 * - 必须实现Serializable接口，否则JPA会报错
 * - equals和hashCode方法必须基于主键字段实现
 * - 主键字段应该是不可变的，避免运行时修改
 * - 在集合中使用时，依赖正确的equals和hashCode实现
 * 
 * @author EcoWiki团队
 * @version 1.0
 * @since 2024-01-01
 */
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
        if (!(o instanceof RolePermissionId)) return false;
        RolePermissionId that = (RolePermissionId) o;
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
