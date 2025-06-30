package com.ecowiki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.entity.UserRole;
import com.ecowiki.entity.UserRoleId;

/**
 * 用户角色关联数据访问接口
 * <p>
 * 继承JpaRepository，提供用户角色关联实体的基础CRUD操作和自定义查询方法。
 * 支持多对多关系管理、角色分配、权限查询等功能。
 * <p>
 * <b>设计说明：</b>
 * - 基于复合主键的多对多关系管理
 * - 支持事务操作，确保数据一致性
 * - 提供丰富的查询方法，满足权限管理需求
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
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
