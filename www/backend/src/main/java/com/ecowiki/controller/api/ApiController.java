package com.ecowiki.controller.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.dto.ApiResponse;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ApiController {
    
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getApiInfo() {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("name", "EcoWiki API");
        apiInfo.put("version", "1.0.0");
        apiInfo.put("description", "EcoWiki 后端 API 服务");
        
        Map<String, Object> endpoints = new HashMap<>();
        
        // 认证相关 API
        Map<String, String> authEndpoints = new HashMap<>();
        authEndpoints.put("POST /api/auth/register", "用户注册");
        authEndpoints.put("POST /api/auth/login", "用户登录");
        authEndpoints.put("GET /api/auth/check-username", "检查用户名可用性");
        authEndpoints.put("GET /api/auth/check-email", "检查邮箱可用性");
        authEndpoints.put("POST /api/auth/forgot-password", "忘记密码");
        authEndpoints.put("POST /api/auth/reset-password", "重置密码");
        authEndpoints.put("GET /api/auth/health", "健康检查");
        
        // 管理员相关 API
        Map<String, String> adminEndpoints = new HashMap<>();
        adminEndpoints.put("GET /api/admin/users", "获取用户列表");
        adminEndpoints.put("PUT /api/admin/users/{id}/group", "更新用户权限组");
        adminEndpoints.put("PUT /api/admin/users/{id}/status", "更新用户状态");
        adminEndpoints.put("DELETE /api/admin/users/{id}", "删除用户");
        adminEndpoints.put("GET /api/admin/stats", "获取系统统计");
        
        endpoints.put("auth", authEndpoints);
        endpoints.put("admin", adminEndpoints);
        
        apiInfo.put("endpoints", endpoints);
        
        return ApiResponse.success(apiInfo);
    }
}
