package com.ecowiki.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    
    /**
     * 根据分组ID查找权限
     */
    List<Permission> findByGroupId(Integer groupId);
    
    /**
     * 根据分组ID查找权限，按排序顺序
     */
    List<Permission> findByGroupIdOrderBySortOrder(Integer groupId);
    
    /**
     * 根据权限key查找权限
     */
    Permission findByPermissionKey(String permissionKey);
    
    /**
     * 检查权限key是否存在（排除指定ID）
     */
    boolean existsByPermissionKeyAndPermissionIdNot(String permissionKey, Integer permissionId);
    
    /**
     * 查找系统内置权限
     */
    List<Permission> findByIsSystemTrue();
    
    /**
     * 查找非系统权限
     */
    List<Permission> findByIsSystemFalse();
    
    /**
     * 根据权限名称模糊查询
     */
    @Query("SELECT p FROM Permission p WHERE p.permissionName LIKE %:name% ORDER BY p.sortOrder")
    List<Permission> findByPermissionNameContaining(@Param("name") String name);
    
    /**
     * 根据权限名称查找权限
     */
    Permission findByPermissionName(String permissionName);

    /**
     * 检查权限名称是否存在（排除指定ID）
     */
    boolean existsByPermissionNameAndPermissionIdNot(String permissionName, Integer permissionId);

    /**
     * 查找所有权限，按ID排序
     */
    List<Permission> findAllByOrderByPermissionId();

    /**
     * 根据角色ID查找权限（通过角色权限关联表）
     */
    @Query("SELECT p FROM Permission p JOIN RolePermission rp ON p.permissionId = rp.permissionId WHERE rp.roleId = :roleId ORDER BY p.permissionId")
    List<Permission> findByRoleId(@Param("roleId") Integer roleId);

    /**
     * 删除指定分组下的所有权限
     */
    void deleteByGroupId(Integer groupId);
}
