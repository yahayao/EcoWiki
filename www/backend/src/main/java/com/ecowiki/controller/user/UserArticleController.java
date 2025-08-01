package com.ecowiki.controller.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.dto.article.ArticleDto;
import com.ecowiki.entity.user.User;
import com.ecowiki.repository.user.UserArticleFavoriteRepository;
import com.ecowiki.repository.user.UserArticleLikeRepository;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.ArticleService;
import com.ecowiki.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户文章交互管理控制器
 * <p>
 * 提供用户个人文章管理相关的RESTful API，包括获取用户收藏的文章、点赞的文章、
 * 发布的文章等个人内容管理功能。支持用户个人中心的文章数据展示。
 * <p>
 * <b>主要功能：</b>
 * - 获取用户收藏的文章列表（分页）
 * - 获取用户点赞的文章列表（分页）
 * - 获取用户发布的文章列表（分页）
 * - 提供文章互动统计信息
 * <p>
 * <b>安全性：</b>
 * - 需要用户登录认证
 * - 基于JWT Token验证用户身份
 * - 用户只能访问自己的数据
 * 
 * @author EcoWiki Development Team
 * @version 1.0
 * @since 2025-08-01
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class UserArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserArticleFavoriteRepository userArticleFavoriteRepository;
    
    @Autowired
    private UserArticleLikeRepository userArticleLikeRepository;
    
    /**
     * 获取当前请求用户实体
     */
    private User getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        
        if (token != null) {
            try {
                String username = jwtUtil.extractUsername(token);
                return userService.findByUsername(username).orElse(null);
            } catch (Exception e) {
                System.err.println("解析用户token失败: " + e.getMessage());
            }
        }
        return null;
    }
    
    /**
     * 从请求中提取JWT token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * 获取用户文章统计信息
     */
    @GetMapping("/article-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserArticleStats(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("请先登录"));
            }
            
            Long userId = currentUser.getUserId();
            
            // 获取统计数据
            long favoriteCount = userArticleFavoriteRepository.countByUserId(userId);
            long likedCount = userArticleLikeRepository.countByUserId(userId);
            
            // 获取用户发布的文章数量（需要在ArticleService中添加相应方法）
            long publishedCount = articleService.countArticlesByAuthor(currentUser.getUsername(), "published");
            long draftCount = articleService.countArticlesByAuthor(currentUser.getUsername(), "draft");
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("favoriteArticles", favoriteCount);
            stats.put("likedArticles", likedCount);
            stats.put("publishedArticles", publishedCount);
            stats.put("draftArticles", draftCount);
            stats.put("totalArticles", publishedCount + draftCount);
            stats.put("totalViews", 0); // 可以后续实现
            stats.put("totalLikes", 0); // 可以后续实现
            
            return ResponseEntity.ok(ApiResponse.success(stats, "获取用户文章统计成功"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取用户文章统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户收藏的文章
     */
    @GetMapping("/favorite-articles")
    public ResponseEntity<ApiResponse<Page<ArticleDto>>> getFavoriteArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("请先登录"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<ArticleDto> articles = articleService.getFavoriteArticlesByUser(currentUser.getUserId(), pageable);
            
            return ResponseEntity.ok(ApiResponse.success(articles, "获取收藏文章成功"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取收藏文章失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户点赞的文章
     */
    @GetMapping("/liked-articles")
    public ResponseEntity<ApiResponse<Page<ArticleDto>>> getLikedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("请先登录"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<ArticleDto> articles = articleService.getLikedArticlesByUser(currentUser.getUserId(), pageable);
            
            return ResponseEntity.ok(ApiResponse.success(articles, "获取点赞文章成功"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取点赞文章失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户发布的文章
     */
    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<Page<ArticleDto>>> getUserArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("请先登录"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            
            // 根据状态获取不同类型的文章
            Page<ArticleDto> articles;
            if ("draft".equals(status)) {
                articles = articleService.getArticlesByAuthorAndStatus(currentUser.getUsername(), "draft", pageable);
            } else {
                articles = articleService.getArticlesByAuthorAndStatus(currentUser.getUsername(), "published", pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(articles, "获取用户文章成功"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取用户文章失败: " + e.getMessage()));
        }
    }
}
