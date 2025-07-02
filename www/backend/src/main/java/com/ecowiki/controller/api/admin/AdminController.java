package com.ecowiki.controller.api.admin;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.dto.UserWithRoleDto;
import com.ecowiki.entity.Permission;
import com.ecowiki.entity.RolePermission;
import com.ecowiki.entity.User;
import com.ecowiki.repository.PermissionRepository;
import com.ecowiki.repository.RolePermissionRepository;
import com.ecowiki.repository.RoleRepository;
import com.ecowiki.repository.UserRepository;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.AdminService;
import com.ecowiki.service.PermissionService;
import com.ecowiki.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 管理员控制器
 * <p>
 * 提供后台管理相关的RESTful API，包括用户管理、角色管理、系统统计等。
 * 依赖权限服务、用户服务、角色仓库等，所有接口均需管理员或超级管理员权限。
 * <p>
 * <b>设计说明：</b>
 * - 采用Spring Boot REST风格，接口安全性依赖JWT认证与权限校验。
 * - 支持分页、排序、权限分级、角色动态管理。
 * - 适用于后台管理系统、权限分配、用户状态维护等场景。
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {
    /**
     * 管理员服务，处理用户和系统管理相关业务
     */
    @Autowired
    private AdminService adminService;
    /**
     * 用户服务，处理用户信息相关操作
     */
    @Autowired
    private UserService userService;
    /**
     * 权限服务，负责权限校验
     */
    @Autowired
    private PermissionService permissionService;
    /**
     * 用户仓库，负责用户数据访问
     */
    @Autowired
    private UserRepository userRepository;
    /**
     * 角色仓库，负责角色数据访问
     */
    @Autowired
    private RoleRepository roleRepository;
    /**
     * JWT工具类，用于token解析
     */
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired 
    private RolePermissionRepository rolePermissionRepository;

    /**
     * 获取当前请求用户实体
     * @param request HTTP请求
     * @return 当前用户对象，若无效则为null
     */
    private User getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            return userOpt.orElse(null);
        }
        return null;
    }

    /**
     * 从请求头提取JWT Token
     * @param request HTTP请求
     * @return Bearer Token字符串，若无则为null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 分页获取所有用户及其角色信息
     * @param page 页码（默认0）
     * @param size 每页数量（默认10）
     * @param sortBy 排序字段（默认id）
     * @param sortDir 排序方向（默认desc）
     * @param request HTTP请求
     * @return 用户分页数据，需管理员权限
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserWithRoleDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<UserWithRoleDto> users = adminService.getAllUsers(pageable);
            return ResponseEntity.ok(ApiResponse.success(users, "获取用户列表成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取用户列表失败: " + e.getMessage()));
        }
    }

    /**
     * 更新指定用户的角色
     * @param userId 用户ID
     * @param roleUpdate 包含新角色的Map（userGroup字段）
     * @param request HTTP请求
     * @return 更新后的用户信息，需管理员权限
     */
    @PutMapping("/users/{userId}/group")
    public ResponseEntity<ApiResponse<User>> updateUserGroup(
            @PathVariable Long userId,
            @RequestBody Map<String, String> roleUpdate,
            HttpServletRequest request) {
        
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            // 防止用户修改自己的角色
            if (currentUser.getUserId().equals(userId)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("不能修改自己的角色权限"));
            }
            
            // 防止修改超级管理员
            Optional<User> targetUserOpt = userService.findById(userId);
            if (targetUserOpt.isPresent()) {
                User targetUser = targetUserOpt.get();
                if (permissionService.isSuperAdmin(targetUser) && !permissionService.isSuperAdmin(currentUser)) {
                    return ResponseEntity.status(403)
                        .body(ApiResponse.error("无法修改超级管理员权限"));
                }
            }
            
            String newRole = roleUpdate.get("userGroup");
            
            // 防止非超级管理员分配superadmin权限
            if ("superadmin".equals(newRole) && !permissionService.isSuperAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("只有超级管理员才能分配超级管理员权限"));
            }
            
            // 使用新的用户角色管理逻辑
            userService.updateUserRole(userId.intValue(), newRole);
            
            // 获取更新后的用户信息
            Optional<User> updatedUserOpt = userService.findById(userId);
            if (updatedUserOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(updatedUserOpt.get(), "权限更新成功"));
            } else {
                throw new RuntimeException("用户不存在");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("权限更新失败: " + e.getMessage()));
        }
    }

    /**
     * 更新指定用户的激活状态
     * @param userId 用户ID
     * @param statusUpdate 包含active字段的Map
     * @param request HTTP请求
     * @return 更新后的用户信息，需管理员权限
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> statusUpdate,
            HttpServletRequest request) {
        
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            // 防止禁用超级管理员
            Optional<User> targetUserOpt = userService.findById(userId);
            if (targetUserOpt.isPresent()) {
                User targetUser = targetUserOpt.get();
                if (permissionService.isSuperAdmin(targetUser)) {
                    return ResponseEntity.status(403)
                        .body(ApiResponse.error("无法禁用超级管理员"));
                }
            }
            
            Boolean active = statusUpdate.get("active");
            User updatedUser = adminService.updateUserStatus(userId, active);
            String message = active ? "用户已启用" : "用户已禁用";
            return ResponseEntity.ok(ApiResponse.success(updatedUser, message));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("状态更新失败: " + e.getMessage()));
        }
    }

    /**
     * 获取系统统计信息
     * @param request HTTP请求
     * @return 统计数据，需管理员权限
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            Map<String, Object> stats = adminService.getSystemStats();
            return ResponseEntity.ok(ApiResponse.success(stats, "获取统计信息成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有角色名称列表
     * @param request HTTP请求
     * @return 角色名称列表，需管理员权限
     */
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<java.util.List<String>>> getAllRoles(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            java.util.List<String> roleNames = roleRepository.findAllRoleNames();
            return ResponseEntity.ok(ApiResponse.success(roleNames, "获取角色列表成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取角色列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有角色详细信息
     * @param request HTTP请求
     * @return 角色实体列表，需管理员权限
     */
    @GetMapping("/roles/details")
    public ResponseEntity<ApiResponse<java.util.List<com.ecowiki.entity.Role>>> getAllRolesDetails(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            java.util.List<com.ecowiki.entity.Role> roles = roleRepository.findAllByOrderByRoleId();
            return ResponseEntity.ok(ApiResponse.success(roles, "获取角色详情成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取角色详情失败: " + e.getMessage()));
        }
    }

    /**
     * 创建新角色
     * @param roleData 包含roleName和description的Map
     * @param request HTTP请求
     * @return 新建角色实体，仅超级管理员可用
     */
    @org.springframework.web.bind.annotation.PostMapping("/roles")
    public ResponseEntity<ApiResponse<com.ecowiki.entity.Role>> createRole(
            @RequestBody Map<String, String> roleData,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isSuperAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要超级管理员权限"));
            }
            
            String roleName = roleData.get("roleName");
            String description = roleData.get("description");
            
            if (roleName == null || roleName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("角色名称不能为空"));
            }
            
            // 检查角色是否已存在
            if (roleRepository.findByRoleName(roleName) != null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("角色名称已存在"));
            }
            
            com.ecowiki.entity.Role role = new com.ecowiki.entity.Role();
            role.setRoleName(roleName);
            role.setDescription(description);
            role.setCreatedAt(java.time.LocalDateTime.now());
            role.setUpdatedAt(java.time.LocalDateTime.now());
            
            com.ecowiki.entity.Role savedRole = roleRepository.save(role);
            return ResponseEntity.ok(ApiResponse.success(savedRole, "角色创建成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("创建角色失败: " + e.getMessage()));
        }
    }

    /**
     * 更新角色信息
     * @param roleId 角色ID
     * @param roleData 包含新roleName和description的Map
     * @param request HTTP请求
     * @return 更新后的角色实体，仅超级管理员可用
     */
    @PutMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse<com.ecowiki.entity.Role>> updateRole(
            @PathVariable Integer roleId,
            @RequestBody Map<String, String> roleData,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isSuperAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要超级管理员权限"));
            }
            
            Optional<com.ecowiki.entity.Role> roleOpt = roleRepository.findById(roleId);
            if (!roleOpt.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("角色不存在"));
            }
            
            com.ecowiki.entity.Role role = roleOpt.get();
            String newRoleName = roleData.get("roleName");
            String newDescription = roleData.get("description");
            
            if (newRoleName != null && !newRoleName.equals(role.getRoleName())) {
                // 检查新角色名是否已存在
                if (roleRepository.findByRoleName(newRoleName) != null) {
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("角色名称已存在"));
                }
                role.setRoleName(newRoleName);
            }
            
            if (newDescription != null) {
                role.setDescription(newDescription);
            }
            
            role.setUpdatedAt(java.time.LocalDateTime.now());
            com.ecowiki.entity.Role updatedRole = roleRepository.save(role);
            
            return ResponseEntity.ok(ApiResponse.success(updatedRole, "角色更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新角色失败: " + e.getMessage()));
        }
    }

    /**
     * 删除角色
     * @param roleId 角色ID
     * @param request HTTP请求
     * @return 删除结果，仅超级管理员可用
     */
    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse<String>> deleteRole(
            @PathVariable Integer roleId,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isSuperAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要超级管理员权限"));
            }
            
            Optional<com.ecowiki.entity.Role> roleOpt = roleRepository.findById(roleId);
            if (!roleOpt.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("角色不存在"));
            }
            
            com.ecowiki.entity.Role role = roleOpt.get();
            
            // 防止删除基础角色
            if ("superadmin".equals(role.getRoleName()) || "admin".equals(role.getRoleName())) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("无法删除系统基础角色"));
            }
            
            // 检查是否有用户使用此角色
            long userCount = userService.countByUserGroup(role.getRoleName());
            if (userCount > 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("无法删除，仍有 " + userCount + " 个用户使用此角色"));
            }
            
            roleRepository.deleteById(roleId);
            return ResponseEntity.ok(ApiResponse.success("角色删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除角色失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有权限列表
     * @param request HTTP请求
     * @return 权限列表，需管理员权限
     */
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<java.util.List<Permission>>> getAllPermissions(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            java.util.List<Permission> permissions = permissionRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success(permissions, "获取权限列表成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取权限列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取角色的权限列表
     * @param roleId 角色ID
     * @param request HTTP请求
     * @return 权限列表，需管理员权限
     */
    @GetMapping("/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<java.util.List<Permission>>> getRolePermissions(
            @PathVariable Integer roleId,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            java.util.List<Permission> permissions = rolePermissionRepository.findPermissionsByRoleId(roleId);
            return ResponseEntity.ok(ApiResponse.success(permissions, "获取角色权限成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取角色权限失败: " + e.getMessage()));
        }
    }

    /**
     * 更新角色的权限配置
     * @param roleId 角色ID
     * @param permissionData 包含permissionIds数组的Map
     * @param request HTTP请求
     * @return 更新结果，需管理员权限
     */
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<String>> updateRolePermissions(
            @PathVariable Integer roleId,
            @RequestBody Map<String, java.util.List<Integer>> permissionData,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            java.util.List<Integer> permissionIds = permissionData.get("permissionIds");
            if (permissionIds == null) {
                permissionIds = new java.util.ArrayList<>();
            }
            
            // 先删除角色的所有权限
            rolePermissionRepository.deleteByRoleId(roleId);
            
            // 重新分配权限
            for (Integer permissionId : permissionIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermission.setCreatedAt(java.time.LocalDateTime.now());
                rolePermissionRepository.save(rolePermission);
            }
            
            return ResponseEntity.ok(ApiResponse.success("角色权限更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新角色权限失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有角色权限关联
     * @param request HTTP请求
     * @return 角色权限关联列表，需管理员权限
     */
    @GetMapping("/role-permissions")
    public ResponseEntity<ApiResponse<java.util.List<RolePermission>>> getAllRolePermissions(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            java.util.List<RolePermission> rolePermissions = rolePermissionRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success(rolePermissions, "获取角色权限关联成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取角色权限关联失败: " + e.getMessage()));
        }
    }

    /**
     * 软删除用户（设置为非激活状态）
     * @param userId 用户ID
     * @param request HTTP请求
     * @return 删除结果，需管理员权限
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<?>> deleteUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            // 防止删除自己
            if (currentUser.getUserId().equals(userId)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("不能删除自己的账户"));
            }
            
            // 防止删除超级管理员
            Optional<User> targetUserOpt = userRepository.findByUserId(userId);
            if (targetUserOpt.isPresent()) {
                User targetUser = targetUserOpt.get();
                if (permissionService.isSuperAdmin(targetUser) && !permissionService.isSuperAdmin(currentUser)) {
                    return ResponseEntity.status(403)
                        .body(ApiResponse.error("无法删除超级管理员"));
                }
            }
            
            // 使用软删除（设置为非激活状态）
            adminService.deleteUser(userId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "用户已禁用"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("禁用用户失败: " + e.getMessage()));
        }
    }

    /**
     * 恢复用户（重新激活）
     * @param userId 用户ID
     * @param request HTTP请求
     * @return 恢复结果，需管理员权限
     */
    @PutMapping("/users/{userId}/restore")
    public ResponseEntity<ApiResponse<?>> restoreUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        
        try {
            User currentUser = getCurrentUser(request);
            if (!permissionService.isAdmin(currentUser)) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            // 恢复用户（重新激活）
            adminService.restoreUser(userId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "用户已恢复"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("恢复用户失败: " + e.getMessage()));
        }
    }
}
