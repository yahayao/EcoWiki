/**
 * 用户角色关联数据访问接口
 * 
 * 功能：
 * - 负责用户角色关联表的数据库操作
 * - 管理用户与角色之间的多对多关系
 * - 提供用户角色分配、查询、统计、删除等功能
 * - 支持基于角色的用户权限管理
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.repository.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.entity.user.UserRole;
import com.ecowiki.entity.user.UserRoleId;
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    /**
     * 根据用户ID查找角色关联
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId")
    List<UserRole> findByUserId(@Param("userId") Integer userId);
    
    /**
     * 根据角色ID查找用户关联
     * @param roleId 角色ID
     * @return 用户角色关联列表
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.roleId = :roleId")
    List<UserRole> findByRoleId(@Param("roleId") Integer roleId);
    
    /**
     * 查找特定用户角色关系
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 用户角色关联（可能为空）
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    Optional<UserRole> findByUserIdAndRoleId(@Param("userId") Integer userId, @Param("roleId") Integer roleId);
    
    /**
     * 删除用户的所有角色
     * @param userId 用户ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId")
    void deleteByUserId(@Param("userId") Integer userId);
    
    /**
     * 删除特定用户角色关系
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    void deleteByUserIdAndRoleId(@Param("userId") Integer userId, @Param("roleId") Integer roleId);
    
    /**
     * 统计拥有某角色的用户数量
     * @param roleId 角色ID
     * @return 用户数量
     */
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.roleId = :roleId")
    Long countByRoleId(@Param("roleId") Integer roleId);
    
    /**
     * 获取用户的主要角色（按创建时间升序）
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId ORDER BY ur.createdAt ASC")
    List<UserRole> findPrimaryRoleByUserId(@Param("userId") Integer userId);
}
