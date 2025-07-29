package com.ecowiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecowiki.entity.user.User;

/**
 * 权限服务类
 * <p>
 * 提供用户权限相关的业务逻辑，包括角色判断、权限校验、用户操作权限判定等。
 * 依赖UserService获取用户角色信息，支持多级角色体系（普通用户、版主、管理员、超级管理员）。
 * <p>
 * <b>设计说明：</b>
 * - 角色体系采用字符串区分，支持灵活扩展。
 * - 所有权限判断均通过本服务集中处理，便于维护和统一管理。
 * - 适用于需要进行权限控制的业务场景，如后台管理、用户操作授权等。
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
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
        if (user == null) return false;
        String roleName = userService.getUserRoleName(user.getUserId().intValue());
        return "admin".equals(roleName) || "superadmin".equals(roleName);
    }

    /**
     * 检查用户是否为超级管理员
     *
     * @param user 待检查的用户实体
     * @return 如果用户为superadmin角色，返回true，否则返回false
     */
    public boolean isSuperAdmin(User user) {
        if (user == null) return false;
        String roleName = userService.getUserRoleName(user.getUserId().intValue());
        return "superadmin".equals(roleName);
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
