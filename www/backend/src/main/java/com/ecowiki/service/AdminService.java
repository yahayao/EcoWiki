/**
 * 管理员服务类
 * 
 * 功能：
 * - 处理管理后台相关的业务逻辑
 * - 提供用户管理、权限分配和系统统计
 * - 支持批量操作和系统级功能
 * - 实现用户角色管理和账号控制
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.dto.user.UserWithRoleDto;
import com.ecowiki.entity.user.User;
import com.ecowiki.repository.user.UserRepository;

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