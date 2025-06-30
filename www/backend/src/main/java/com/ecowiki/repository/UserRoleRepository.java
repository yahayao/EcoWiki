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

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    
    // 根据用户ID查找角色
    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId")
    List<UserRole> findByUserId(@Param("userId") Integer userId);
    
    // 根据角色ID查找用户
    @Query("SELECT ur FROM UserRole ur WHERE ur.roleId = :roleId")
    List<UserRole> findByRoleId(@Param("roleId") Integer roleId);
    
    // 查找特定用户角色关系
    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    Optional<UserRole> findByUserIdAndRoleId(@Param("userId") Integer userId, @Param("roleId") Integer roleId);
    
    // 删除用户的所有角色
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId")
    void deleteByUserId(@Param("userId") Integer userId);
    
    // 删除特定用户角色关系
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    void deleteByUserIdAndRoleId(@Param("userId") Integer userId, @Param("roleId") Integer roleId);
    
    // 统计拥有某角色的用户数量
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.roleId = :roleId")
    Long countByRoleId(@Param("roleId") Integer roleId);
    
    // 获取用户的主要角色（假设一个用户只有一个主要角色）
    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId ORDER BY ur.createdAt ASC")
    List<UserRole> findPrimaryRoleByUserId(@Param("userId") Integer userId);
}
