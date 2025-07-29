package com.ecowiki.service;

/**
 * 管理员服务类
 *
 * 负责处理管理后台相关的业务逻辑，包括用户管理、权限分配、系统统计等。
 * 该服务为管理员提供批量操作和系统级功能的支持。
 *
 * 主要功能：
 * - 分页获取所有用户及其角色信息
 * - 更新用户角色（权限组）
 * - 启用/禁用用户账号
 * - 获取系统统计数据（用户数、活跃数、各角色分布）
 * - 软删除用户（将用户设为不可用）
 *
 * 依赖组件：
 * - UserRepository：用户数据访问层
 * - UserService：用户业务逻辑层
 *
 * 设计说明：
 * - 角色分配和统计均通过UserService统一处理
 * - 支持系统级统计和批量管理操作
 * - 采用事务管理，保证批量操作的原子性
 *
 * @author EcoWiki Team
 * @version 2.0
 * @since 2025-06-30
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.dto.user.UserWithRoleDto;
import com.ecowiki.entity.User;
import com.ecowiki.repository.UserRepository;

@Service
@Transactional
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    // 获取所有用户（分页，包括软删除的用户）
    public Page<UserWithRoleDto> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAllForAdmin(pageable);
        return userPage.map(user -> {
            String roleName = userService.getUserRoleName(user.getUserId().intValue());
            return new UserWithRoleDto(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                roleName
            );
        });
    }
    
    // 更新用户权限组
    public User updateUserGroup(Long userId, String newUserGroup) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        // 使用UserService更新用户角色
        userService.updateUserRole(userId.intValue(), newUserGroup);
        return user;
    }
    
    // 更新用户状态
    public User updateUserStatus(Long userId, Boolean active) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setActive(active);
        return userRepository.save(user);
    }
    
    // 获取系统统计信息
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        // 活跃用户数
        long activeUsers = userRepository.countByActiveTrue();
        stats.put("activeUsers", activeUsers);
        
        // 各权限组用户数（通过UserService统计）
        stats.put("userCount", userService.countByUserGroup("user"));
        stats.put("moderatorCount", userService.countByUserGroup("moderator"));
        stats.put("adminCount", userService.countByUserGroup("admin"));
        stats.put("superadminCount", userService.countByUserGroup("superadmin"));
        
        return stats;
    }
    
    // 软删除用户（设置为非激活状态）
    public void deleteUser(Long userId) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setActive(false);
        userRepository.save(user);
    }
    
    // 恢复用户（重新激活）
    public void restoreUser(Long userId) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setActive(true);
        userRepository.save(user);
    }
}