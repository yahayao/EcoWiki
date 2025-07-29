package com.ecowiki.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.user.Role;

/**
 * 角色数据访问接口
 * <p>
 * 继承JpaRepository，提供角色实体的基础CRUD操作和自定义查询方法。
 * 支持角色名称查询、角色列表获取等功能。
 * <p>
 * <b>设计说明：</b>
 * - 基于Spring Data JPA，自动生成基础CRUD方法
 * - 支持角色管理、权限分配等业务场景
 * - 提供有序的角色查询，便于前端展示
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    /**
     * 查找所有角色名称（按ID排序）
     * @return 角色名称列表
     */
    @Query("SELECT r.roleName FROM Role r ORDER BY r.roleId")
    List<String> findAllRoleNames();
    
    /**
     * 根据角色名称查找角色
     * @param roleName 角色名称
     * @return 角色实体（可能为null）
     */
    Role findByRoleName(String roleName);
    
    /**
     * 查找所有角色（按ID排序）
     * @return 角色实体列表
     */
    List<Role> findAllByOrderByRoleId();
}
