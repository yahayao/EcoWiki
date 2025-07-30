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

/**
 * 角色权限关联数据访问接口
 * 
 * 负责角色权限关联表的数据库操作，管理角色与权限之间的多对多关系。
 * 提供角色权限分配、查询、统计、删除等完整的数据访问功能。
 * 
 * 主要功能：
 * - 关联管理：创建、查询、删除角色权限关联
 * - 权限分配：为角色分配和撤销权限
 * - 关联查询：根据角色或权限查询关联关系
 * - 存在性检查：检查角色是否拥有特定权限
 * - 统计功能：统计角色权限数量和使用情况
 * - 批量操作：支持批量删除和管理操作
 * 
 * 核心方法：
 * - findByRoleId：查询角色拥有的所有权限关联
 * - findByPermissionId：查询使用特定权限的所有角色关联
 * - existsByRoleIdAndPermissionId：检查角色是否拥有指定权限
 * - countByPermissionId：统计使用指定权限的角色数量
 * - countByRoleId：统计角色拥有的权限数量
 * - deleteByRoleId：删除角色的所有权限（角色删除时使用）
 * - deleteByPermissionId：删除权限的所有关联（权限删除时使用）
 * - deleteByRoleIdAndPermissionId：删除特定的角色权限关联
 * - findPermissionsByRoleId：查询角色的权限详情列表
 * 
 * 数据库表：role_permissions
 * 主键类型：RolePermissionId (复合主键：roleId + permissionId)
 * 
 * 使用场景：
 * - 权限管理：动态分配和管理用户角色权限
 * - 访问控制：基于角色的访问控制(RBAC)实现
 * - 权限验证：验证用户是否具有执行特定操作的权限
 * - 权限审计：追踪和分析权限分配情况
 * - 批量权限操作：支持批量分配和撤销权限
 * 
 * 技术特性：
 * - 复合主键：使用RolePermissionId作为复合主键
 * - 事务管理：删除操作使用@Transactional和@Modifying注解
 * - 关联查询：通过JOIN查询获取权限详细信息
 * - 参数绑定：使用@Param注解进行参数绑定
 * - 存在性检查：提供高效的存在性验证方法
 * 
 * 设计模式：
 * - 多对多关系：角色和权限的多对多关联映射
 * - 关联表模式：通过中间表管理复杂关系
 * - 统计查询：提供count方法进行数量统计
 * - 批量删除：支持基于条件的批量删除操作
 * 
 * 注意事项：
 * - 删除操作需要@Modifying和@Transactional注解
 * - 批量删除前应检查数据依赖关系
 * - 复合主键操作需要使用RolePermissionId对象
 * - 权限变更应该记录操作日志便于审计
 * - 高频查询建议进行缓存优化
 * 
 * @author EcoWiki团队
 * @version 1.0
 * @since 2024-01-01
 */
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
