/**
 * 权限服务类
 * 
 * 功能：
 * - 提供用户权限相关的业务逻辑处理
 * - 支持多级角色体系和权限校验
 * - 实现用户操作权限判定功能
 * - 提供角色管理和权限分配服务
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecowiki.entity.user.User;

@Service
public class PermissionService {
    /**
     * 用户服务依赖，用于获取用户角色信息
     */
    @Autowired
    private UserService userService;

    /**
     * 检查用户是否为管理员
     *
     * @param user 待检查的用户实体
     * @return 如果用户为admin或superadmin角色，返回true，否则返回false
     */
    public boolean isAdmin(User user) {
    // reuse hasPermission which already treats superadmin as having all permissions
    return hasPermission(user, "admin");
    }

    /**
     * 检查用户是否为超级管理员
     *
     * @param user 待检查的用户实体
     * @return 如果用户为superadmin角色，返回true，否则返回false
     */
    public boolean isSuperAdmin(User user) {
        return hasPermission(user, "user");
    }

    /**
     * 检查用户是否拥有指定权限
     *
     * @param user         待检查的用户实体
     * @param requiredRole 需要判断的权限角色（如admin、moderator、user等）
     * @return 用户是否拥有该权限
     * <p>
     * <b>权限体系说明：</b>
     * - superadmin：拥有所有权限
     * - admin：拥有admin、moderator、user权限
     * - moderator：拥有moderator、user权限
     * - user：仅拥有user权限
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
     * 检查当前用户是否可以修改目标用户信息
     *
     * @param currentUser 当前操作用户
     * @param targetUser  目标被操作用户
     * @return 是否有权限修改
     * <p>
     * <b>规则说明：</b>
     * - 超级管理员可修改除其他超级管理员外的所有用户（可修改自己）
     * - 管理员可修改普通用户和版主
     * - 其他用户只能修改自己
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
