/**
 * 角色数据访问层接口
 * 
 * 功能包括：
 * - 角色实体的CRUD操作
 * - 角色名称查询和验证
 * - 角色权限关联查询
 * - 角色列表获取和统计
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.repository.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.user.Role;

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
