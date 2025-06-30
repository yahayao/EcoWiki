package com.ecowiki.controller.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.dto.ApiResponse;

/**
 * API信息控制器
 * <p>
 * 提供EcoWiki后端API的基本信息和接口文档说明，便于前端或第三方系统自动发现和集成。
 * <p>
 * <b>设计说明：</b>
 * - 采用REST风格，返回统一的ApiResponse结构。
 * - 支持跨域访问，便于前后端分离开发。
 * - 适用于API自描述、接口调试、前端自动生成文档等场景。
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
@RestController
@RequestMapping("")
@CrossOrigin(origins = "http://localhost:5173")
public class ApiController {
    /**
     * 获取API基本信息及主要接口文档
     *
     * @return 包含API名称、版本、描述、主要端点分组的响应体
     * <p>
     * <b>返回结构说明：</b>
     * - name: API名称
     * - version: 版本号
     * - description: 简要描述
     * - endpoints: 按功能分组的接口说明（auth/admin/articles等）
     *
     * <b>使用场景：</b>
     * - 前端自动生成接口文档
     * - 第三方系统集成API发现
     * - 健康检查与API元数据获取
     */
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
        
        // 文章相关 API
        Map<String, String> articleEndpoints = new HashMap<>();
        articleEndpoints.put("POST /api/articles", "创建文章");
        articleEndpoints.put("GET /api/articles", "获取文章列表（分页）");
        articleEndpoints.put("GET /api/articles/{id}", "根据ID获取文章");
        articleEndpoints.put("PUT /api/articles/{id}", "更新文章");
        articleEndpoints.put("DELETE /api/articles/{id}", "删除文章");
        articleEndpoints.put("GET /api/articles/category/{category}", "根据分类获取文章");
        articleEndpoints.put("GET /api/articles/author/{author}", "根据作者获取文章");
        articleEndpoints.put("GET /api/articles/search", "搜索文章");
        articleEndpoints.put("GET /api/articles/tag/{tag}", "根据标签获取文章");
        articleEndpoints.put("GET /api/articles/popular", "获取热门文章");
        articleEndpoints.put("GET /api/articles/latest", "获取最新文章");
        articleEndpoints.put("POST /api/articles/{id}/like", "点赞文章");
        articleEndpoints.put("DELETE /api/articles/{id}/like", "取消点赞");
        articleEndpoints.put("PUT /api/articles/{id}/comments", "更新评论数");
        articleEndpoints.put("GET /api/articles/statistics", "获取文章统计");
        
        endpoints.put("auth", authEndpoints);
        endpoints.put("admin", adminEndpoints);
        endpoints.put("articles", articleEndpoints);
        
        apiInfo.put("endpoints", endpoints);
        
        return ApiResponse.success(apiInfo);
    }
}
