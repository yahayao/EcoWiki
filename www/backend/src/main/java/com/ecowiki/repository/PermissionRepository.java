package com.ecowiki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.Permission;

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
