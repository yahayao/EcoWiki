package com.ecowiki.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    // 查找所有角色名称
    @Query("SELECT r.roleName FROM Role r ORDER BY r.roleId")
    List<String> findAllRoleNames();
    
    // 根据角色名称查找角色
    Role findByRoleName(String roleName);
    
    // 查找所有激活的角色（如果有激活状态字段）
    List<Role> findAllByOrderByRoleId();
}
