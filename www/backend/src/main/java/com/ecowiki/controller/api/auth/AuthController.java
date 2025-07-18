package com.ecowiki.controller.api.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.dto.LoginRequest;
import com.ecowiki.dto.ResetPasswordRequest;
import com.ecowiki.dto.UpdateProfileRequest;
import com.ecowiki.dto.UserRegistrationDto;
import com.ecowiki.entity.User;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.UserService;

import jakarta.validation.Valid;

/**
 * 认证控制器
 * 
 * 处理用户认证相关的所有API请求，包括用户注册、登录、密码重置等功能。
 * 这是前端与后端认证系统交互的主要入口点，负责用户身份验证和权限管理。
 * 
 * 主要功能：
 * - 用户注册：新用户账号创建和信息验证
 * - 用户登录：身份验证和JWT令牌生成
 * - 用户名检查：实时验证用户名是否可用
 * - 邮箱检查：实时验证邮箱是否已注册
 * - 密码重置：通过邮箱和安全问题重置密码
 * - 用户信息：返回当前登录用户的详细信息
 * 
 * 安全特性：
 * - 跨域资源共享(CORS)配置：支持前后端分离架构
 * - 请求数据验证：使用@Valid注解进行参数校验
 * - JWT令牌认证：生成和验证JSON Web Token
 * - 密码安全处理：安全的密码存储和验证机制
 * - 防暴力破解：限制登录尝试次数和频率
 * 
 * API端点：
 * - GET /auth/check-username?username={username} - 检查用户名可用性
 * - GET /auth/check-email?email={email} - 检查邮箱可用性
 * - POST /auth/register - 用户注册
 * - POST /auth/login - 用户登录
 * - POST /auth/forgot-password - 忘记密码申请
 * - POST /auth/reset-password - 重置密码
 * - GET /auth/user-info?token={token} - 获取用户信息
 * 
 * 依赖组件：
 * - UserService：用户业务逻辑处理
 * - JwtUtil：JWT令牌工具类
 * 
 * 响应格式：
 * - 统一使用ApiResponse包装返回数据
 * - 成功响应包含data字段
 * - 错误响应包含错误码和错误信息
 * 
 * 使用场景：
 * - 用户注册和登录流程
 * - 前端表单验证（用户名、邮箱唯一性）
 * - 用户密码找回和重置
 * - 用户身份认证和权限获取
 * 
 * 注意事项：
 * - 所有密码相关操作需要额外的安全验证
 * - JWT令牌应设置合理的过期时间
 * - 用户输入需要进行XSS防护处理
 * - 敏感操作应记录操作日志
 * 
 * @author EcoWiki团队
 * @version 2.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")  // 允许任意前端跨域访问
@Validated  // 启用请求参数验证
public class AuthController {
    
    /**
     * 用户服务
     * 处理用户相关的业务逻辑
     */
    @Autowired
    private UserService userService;
    
    /**
     * JWT工具类
     * 用于生成和验证JWT令牌
     */
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 检查用户名可用性
     * 
     * 在用户注册过程中实时检查用户名是否已被使用。
     * 支持前端的实时验证功能。
     * 
     * @param username 要检查的用户名
     * @return API响应，包含可用性状态
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsername(@RequestParam String username) {
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", userService.isUsernameAvailable(username));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 检查邮箱可用性
     * 
     * 在用户注册过程中实时检查邮箱地址是否已被使用。
     * 支持前端的实时验证功能。
     * 
     * @param email 要检查的邮箱地址
     * @return API响应，包含可用性状态
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmail(@RequestParam String email) {
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", userService.isEmailAvailable(email));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerUser(@Valid @RequestBody UserRegistrationDto dto) {
        try {
            User user = userService.registerUser(dto);
            
            // 生成JWT token
            String token = jwtUtil.generateToken(user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
            
            Map<String, Object> result = new HashMap<>();
            result.put("user", createUserResponse(user));
            result.put("token", token);
            result.put("refreshToken", refreshToken);
            
            return ResponseEntity.ok(ApiResponse.success(result, "注册成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> loginUser(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.authenticateUser(request);
            
            // 生成JWT token
            String token = jwtUtil.generateToken(user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
            
            Map<String, Object> result = new HashMap<>();
            result.put("user", createUserResponse(user));
            result.put("token", token);
            result.put("refreshToken", refreshToken);
            
            return ResponseEntity.ok(ApiResponse.success(result, "登录成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            // 支持用户名或邮箱登录
            User user = null;
            if (resetPasswordRequest.getEmail() != null && !resetPasswordRequest.getEmail().isEmpty()) {
                user = userService.findUserByEmail(resetPasswordRequest.getEmail());
            } else if (resetPasswordRequest.getUsername() != null && !resetPasswordRequest.getUsername().isEmpty()) {
                user = userService.findByUsername(resetPasswordRequest.getUsername()).orElse(null);
            }
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户不存在"));
            }
            
            if (!resetPasswordRequest.getSecurityAnswer().equals(user.getSecurityAnswer())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("安全问题答案错误"));
            }
            
            boolean success = userService.resetPassword(user, resetPasswordRequest.getNewPassword());
            if (success) {
                // 密码重置成功后，生成新的JWT token并返回认证信息
                String token = jwtUtil.generateToken(user.getUsername());
                String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
                
                Map<String, Object> result = new HashMap<>();
                result.put("user", createUserResponse(user));
                result.put("token", token);
                result.put("refreshToken", refreshToken);
                
                return ResponseEntity.ok(ApiResponse.success(result, "密码重置成功"));
            } else {
                return ResponseEntity.internalServerError().body(ApiResponse.error("密码重置失败"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "OK");
        result.put("message", "EcoWiki API is running");
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 获取当前用户信息
     * 
     * 通过JWT令牌获取当前登录用户的详细信息。
     * 用于前端获取最新的用户数据，支持用户资料页面等功能。
     * 
     * @param request HTTP请求，包含Authorization头部
     * @return API响应，包含用户详细信息
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(jakarta.servlet.http.HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("未提供认证令牌"));
            }
            
            // 验证令牌
            if (!jwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("令牌无效或已过期"));
            }
            
            // 从令牌中提取用户名
            String username = jwtUtil.extractUsername(token);
            if (username == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("无法从令牌中获取用户信息"));
            }
            
            // 获取用户信息
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("用户不存在"));
            }
            
            User user = userOpt.get();
            
            // 检查用户是否被禁用
            if (!user.getActive()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("账户已被禁用"));
            }
            
            // 返回用户信息
            Map<String, Object> result = createUserResponse(user);
            return ResponseEntity.ok(ApiResponse.success(result, "获取用户信息成功"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("获取用户信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新用户个人资料
     * @param request HTTP请求
     * @param profileData 要更新的个人资料数据
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Object>> updateProfile(
            jakarta.servlet.http.HttpServletRequest request,
            @Valid @RequestBody UpdateProfileRequest profileData) {
        try {
            // 从请求头提取JWT Token
            String token = extractTokenFromRequest(request);
            
            if (token == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("缺少认证令牌"));
            }
            
            // 验证JWT令牌并获取用户名
            String username = jwtUtil.extractUsername(token);
            if (username == null || !jwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("认证令牌无效"));
            }
            
            // 根据用户名查找用户
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("用户不存在"));
            }
            
            User user = userOpt.get();
            
            // 更新用户信息
            if (profileData.getUsername() != null && !profileData.getUsername().trim().isEmpty()) {
                // 检查新用户名是否已存在（排除当前用户）
                if (!profileData.getUsername().equals(user.getUsername()) && 
                    !userService.isUsernameAvailable(profileData.getUsername())) {
                    return ResponseEntity.status(400)
                        .body(ApiResponse.error("用户名已存在"));
                }
                user.setUsername(profileData.getUsername().trim());
            }
            
            if (profileData.getFullName() != null && !profileData.getFullName().trim().isEmpty()) {
                user.setFullName(profileData.getFullName().trim());
            }
            
            if (profileData.getEmail() != null && !profileData.getEmail().trim().isEmpty()) {
                // 检查新邮箱是否已存在（排除当前用户）
                if (!profileData.getEmail().equals(user.getEmail()) && 
                    !userService.isEmailAvailable(profileData.getEmail())) {
                    return ResponseEntity.status(400)
                        .body(ApiResponse.error("邮箱已被注册"));
                }
                user.setEmail(profileData.getEmail().trim());
            }
            
            // 保存更新后的用户信息
            User updatedUser = userService.saveUser(user);
            
            // 返回更新后的用户信息
            Map<String, Object> result = createUserResponse(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(result, "个人资料更新成功"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("系统繁忙，请稍后重试"));
        }
    }
    
    /**
     * 从请求头提取JWT Token
     * @param request HTTP请求
     * @return Bearer Token字符串，若无则为null
     */
    private String extractTokenFromRequest(jakarta.servlet.http.HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    // 创建用户响应对象（不包含密码等敏感信息）
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getUserId());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("fullName", user.getFullName());
        // 角色信息现在通过UserService获取
        userResponse.put("userGroup", userService.getUserRoleName(user.getUserId().intValue()));
        userResponse.put("active", user.getActive());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("updatedAt", user.getUpdatedAt());
        return userResponse;
    }
}
