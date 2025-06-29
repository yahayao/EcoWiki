package com.ecowiki.controller.api.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.dto.ForgotPasswordRequest;
import com.ecowiki.dto.LoginRequest;
import com.ecowiki.dto.ResetPasswordRequest;
import com.ecowiki.dto.UserRegistrationDto;
import com.ecowiki.entity.User;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsername(@RequestParam String username) {
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", userService.isUsernameAvailable(username));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
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
    
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        User user = userService.findUserByEmail(forgotPasswordRequest.getEmail());
        if (user == null) {
            return ((BodyBuilder) ResponseEntity.notFound()).body(ApiResponse.error("用户不存在"));
        }
        String securityQuestion = userService.getSecurityQuestion(user);
        return ResponseEntity.ok(ApiResponse.success(securityQuestion));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        User user = userService.findUserByEmail(resetPasswordRequest.getEmail());
        if (user == null || !resetPasswordRequest.getAnswer().equals(user.getSecurityAnswer())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("安全问题答案错误"));
        }
        boolean success = userService.resetPassword(user, resetPasswordRequest.getNewPassword());
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("密码重置成功"));
        } else {
            return ResponseEntity.internalServerError().body(ApiResponse.error("密码重置失败"));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "OK");
        result.put("message", "EcoWiki API is running");
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    // 创建用户响应对象（不包含密码等敏感信息）
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getUserId());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("fullName", user.getFullName());
        userResponse.put("userGroup", user.getUserGroup());
        userResponse.put("active", user.getActive());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("updatedAt", user.getUpdatedAt());
        return userResponse;
    }
}
