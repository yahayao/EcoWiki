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
import com.ecowiki.entity.User;
import com.ecowiki.repository.RoleRepository;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.AdminService;
import com.ecowiki.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // 获取当前用户
    private User getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userService.findByUsername(username);
            return userOpt.orElse(null);
        }
        return null;
    }
    
    // 提取token
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    // 获取用户列表
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {
        
        try {
            User currentUser = getCurrentUser(request);
            if (!currentUser.isAdmin()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<User> users = adminService.getAllUsers(pageable);
            return ResponseEntity.ok(ApiResponse.success(users, "获取用户列表成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取用户列表失败: " + e.getMessage()));
        }
    }
    
    // 更新用户权限组
    @PutMapping("/users/{userId}/group")
    public ResponseEntity<ApiResponse<User>> updateUserGroup(
            @PathVariable Long userId,
            @RequestBody Map<String, String> groupUpdate,
            HttpServletRequest request) {
        
        try {
            User currentUser = getCurrentUser(request);
            if (!currentUser.isAdmin()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            // 防止修改超级管理员
            Optional<User> targetUserOpt = userService.findById(userId);
            if (targetUserOpt.isPresent()) {
                User targetUser = targetUserOpt.get();
                if (targetUser.isSuperAdmin() && !currentUser.isSuperAdmin()) {
                    return ResponseEntity.status(403)
                        .body(ApiResponse.error("无法修改超级管理员权限"));
                }
            }
            
            String newGroup = groupUpdate.get("userGroup");
            User updatedUser = adminService.updateUserGroup(userId, newGroup);
            return ResponseEntity.ok(ApiResponse.success(updatedUser, "权限更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("权限更新失败: " + e.getMessage()));
        }
    }
    
    // 更新用户状态
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> statusUpdate,
            HttpServletRequest request) {
        
        try {
            User currentUser = getCurrentUser(request);
            if (!currentUser.isAdmin()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            // 防止禁用超级管理员
            Optional<User> targetUserOpt = userService.findById(userId);
            if (targetUserOpt.isPresent()) {
                User targetUser = targetUserOpt.get();
                if (targetUser.isSuperAdmin()) {
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
    
    // 获取系统统计
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!currentUser.isAdmin()) {
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
    
    // 获取所有角色列表
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<java.util.List<String>>> getAllRoles(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!currentUser.isAdmin()) {
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
    
    // 获取所有角色详细信息
    @GetMapping("/roles/details")
    public ResponseEntity<ApiResponse<java.util.List<com.ecowiki.entity.Role>>> getAllRolesDetails(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!currentUser.isAdmin()) {
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
    
    // 创建新角色
    @org.springframework.web.bind.annotation.PostMapping("/roles")
    public ResponseEntity<ApiResponse<com.ecowiki.entity.Role>> createRole(
            @RequestBody Map<String, String> roleData,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!currentUser.isSuperAdmin()) {
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
    
    // 更新角色
    @PutMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse<com.ecowiki.entity.Role>> updateRole(
            @PathVariable Integer roleId,
            @RequestBody Map<String, String> roleData,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!currentUser.isSuperAdmin()) {
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
    
    // 删除角色
    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse<String>> deleteRole(
            @PathVariable Integer roleId,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!currentUser.isSuperAdmin()) {
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
}
