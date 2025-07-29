package com.ecowiki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.user.Permission;

/**
 * 权限数据访问接口
 * 
 * 负责权限实体的数据库操作，提供权限管理相关的查询和操作方法。
 * 继承JpaRepository，自动获得基础的CRUD操作，同时定义了权限管理特有的查询方法。
 * 
 * 主要功能：
 * - 权限基础操作：增删改查权限信息
 * - 权限查询：根据权限名称、ID等条件查询权限
 * - 角色权限关联：查询特定角色拥有的权限列表
 * - 模糊搜索：支持权限名称的模糊查询功能
 * - 权限排序：提供按权限ID排序的查询方法
 * 
 * 查询方法：
 * - findByPermissionName：根据权限名称精确查找权限
 * - findAllByOrderByPermissionId：获取所有权限并按ID排序
 * - findByRoleId：根据角色ID查询该角色拥有的所有权限
 * - findByPermissionNameContaining：权限名称模糊搜索
 * 
 * 数据库表：permission
 * 主键类型：Integer (权限ID)
 * 
 * 使用场景：
 * - 权限管理系统：管理系统中的所有权限定义
 * - 角色分配：为角色分配和查询权限
 * - 权限验证：验证用户是否拥有特定权限
 * - 权限搜索：在管理界面搜索和浏览权限
 * 
 * 技术特性：
 * - Spring Data JPA：自动生成基础CRUD方法
 * - 自定义查询：使用@Query注解定义复杂查询
 * - 关联查询：通过JOIN查询角色权限关联
 * - 参数绑定：使用@Param注解绑定查询参数
 * 
 * 注意事项：
 * - 权限名称应保持唯一性，避免重复定义
 * - 删除权限前应检查是否有角色关联
 * - 批量操作时注意事务管理
 * - 权限查询结果建议进行缓存优化
 * 
 * @author EcoWiki团队
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    
    /**
     * 根据权限名称查找权限
     */
    Optional<Permission> findByPermissionName(String permissionName);
    
    /**
     * 获取所有权限，按权限ID排序
     */
    List<Permission> findAllByOrderByPermissionId();
    
    /**
     * 根据角色ID获取权限列表
     */
    @Query("SELECT p FROM Permission p JOIN RolePermission rp ON p.permissionId = rp.permissionId WHERE rp.roleId = :roleId ORDER BY p.permissionId")
    List<Permission> findByRoleId(@Param("roleId") Integer roleId);
    
    /**
     * 根据权限名称模糊查询
     */
    @Query("SELECT p FROM Permission p WHERE p.permissionName LIKE %:name% ORDER BY p.permissionId")
    List<Permission> findByPermissionNameContaining(@Param("name") String name);
}
