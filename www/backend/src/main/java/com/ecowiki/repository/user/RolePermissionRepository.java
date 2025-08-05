/**
 * 角色权限关联数据访问接口
 * 
 * 功能：
 * - 负责角色权限关联表的数据库操作
 * - 管理角色与权限之间的多对多关系
 * - 提供角色权限分配、查询、统计、删除等功能
 * - 支持基于角色的访问控制(RBAC)实现
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.repository.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.entity.user.RolePermission;
import com.ecowiki.entity.user.RolePermissionId;
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    
    /**
     * 根据角色ID查找权限关联
     */
    List<RolePermission> findByRoleId(Integer roleId);
    
    /**
     * 根据权限ID查找角色关联
     */
    List<RolePermission> findByPermissionId(Integer permissionId);
    
    /**
     * 检查角色是否拥有指定权限
     */
    boolean existsByRoleIdAndPermissionId(Integer roleId, Integer permissionId);
    
    /**
     * 统计使用指定权限的角色数量
     */
    long countByPermissionId(Integer permissionId);
    
    /**
     * 统计角色拥有的权限数量
     */
    long countByRoleId(Integer roleId);
    
    /**
     * 删除角色的所有权限
     */
    @Modifying
    @Transactional
    void deleteByRoleId(Integer roleId);
    
    /**
     * 删除指定权限的所有角色关联
     */
    @Modifying
    @Transactional
    void deleteByPermissionId(Integer permissionId);
    
    /**
     * 删除特定的角色权限关联
     */
    @Modifying
    @Transactional
    void deleteByRoleIdAndPermissionId(Integer roleId, Integer permissionId);
    
    /**
     * 根据角色ID查找权限详情
     */
    @Query("SELECT p FROM Permission p JOIN RolePermission rp ON p.permissionId = rp.permissionId WHERE rp.roleId = :roleId ORDER BY p.permissionId")
    List<com.ecowiki.entity.user.Permission> findPermissionsByRoleId(@Param("roleId") Integer roleId);
}
