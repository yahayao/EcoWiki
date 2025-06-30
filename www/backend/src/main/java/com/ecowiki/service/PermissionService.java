package com.ecowiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecowiki.entity.User;

@Service
public class PermissionService {
    
    @Autowired
    private UserService userService;
    
    /**
     * 检查用户是否为管理员
     */
    public boolean isAdmin(User user) {
        if (user == null) return false;
        String roleName = userService.getUserRoleName(user.getUserId().intValue());
        return "admin".equals(roleName) || "superadmin".equals(roleName);
    }
    
    /**
     * 检查用户是否为超级管理员
     */
    public boolean isSuperAdmin(User user) {
        if (user == null) return false;
        String roleName = userService.getUserRoleName(user.getUserId().intValue());
        return "superadmin".equals(roleName);
    }
    
    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(User user, String requiredRole) {
        if (user == null) return false;
        String userRole = userService.getUserRoleName(user.getUserId().intValue());
        
        // 超级管理员拥有所有权限
        if ("superadmin".equals(userRole)) return true;
        
        // 管理员权限检查
        if ("admin".equals(userRole)) {
            return "admin".equals(requiredRole) || "moderator".equals(requiredRole) || "user".equals(requiredRole);
        }
        
        // 版主权限检查
        if ("moderator".equals(userRole)) {
            return "moderator".equals(requiredRole) || "user".equals(requiredRole);
        }
        
        // 普通用户权限检查
        if ("user".equals(userRole)) {
            return "user".equals(requiredRole);
        }
        
        return false;
    }
    
    /**
     * 检查用户是否可以修改目标用户
     */
    public boolean canModifyUser(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) return false;
        
        // 超级管理员可以修改任何人（除了其他超级管理员）
        if (isSuperAdmin(currentUser)) {
            return !isSuperAdmin(targetUser) || currentUser.getUserId().equals(targetUser.getUserId());
        }
        
        // 管理员可以修改普通用户和版主
        if (isAdmin(currentUser)) {
            return !isAdmin(targetUser);
        }
        
        // 其他用户只能修改自己
        return currentUser.getUserId().equals(targetUser.getUserId());
    }
}
