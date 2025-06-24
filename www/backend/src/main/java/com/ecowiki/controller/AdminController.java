package com.ecowiki.controller;

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
    
    // 删除用户
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (!currentUser.isAdmin()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("权限不足，需要管理员权限"));
            }
            
            // 防止删除超级管理员
            Optional<User> targetUserOpt = userService.findById(userId);
            if (targetUserOpt.isPresent()) {
                User targetUser = targetUserOpt.get();
                if (targetUser.isSuperAdmin()) {
                    return ResponseEntity.status(403)
                        .body(ApiResponse.error("无法删除超级管理员"));
                }
            }
            
            adminService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success("用户删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除用户失败: " + e.getMessage()));
        }
    }
}