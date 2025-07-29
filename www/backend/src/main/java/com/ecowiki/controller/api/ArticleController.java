package com.ecowiki.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.dto.article.ArticleCreateRequest;
import com.ecowiki.dto.article.ArticleDto;
import com.ecowiki.dto.article.ArticleStatisticsDto;
import com.ecowiki.dto.article.ArticleUpdateRequest;
import com.ecowiki.entity.User;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.ArticleService;
import com.ecowiki.service.ArticleVersionService;
import com.ecowiki.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 文章控制器
 * <p>
 * 提供EcoWiki文章相关的RESTful API，包括文章的增删改查、分类、标签、作者、搜索、点赞、评论统计等。
 * 依赖ArticleService进行业务处理，所有接口均返回统一的ApiResponse结构。
 * <p>
 * <b>设计说明：</b>
 * - 采用Spring Boot REST风格，支持分页、排序、条件过滤。
 * - 适用于Wiki内容管理、文章展示、社区互动等场景。
 * - 支持前后端分离，接口安全性可扩展。
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
@RestController
@RequestMapping("/articles")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@Validated
public class ArticleController {
    /**
     * 文章服务，处理所有文章相关业务
     */
    @Autowired
    private ArticleService articleService;
    
    /**
     * 用户服务，用于获取当前用户信息
     */
    @Autowired
    private UserService userService;
    
    /**
     * JWT工具类，用于解析用户token
     */
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 文章版本服务，用于测试版本创建
     */
    @Autowired
    private ArticleVersionService articleVersionService;

    /**
     * 获取当前请求用户实体
     * @param request HTTP请求
     * @return 当前用户对象，若无效则为null
     */
    private User getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        System.out.println("=== getCurrentUser Debug ===");
        System.out.println("Authorization header: " + request.getHeader("Authorization"));
        System.out.println("Token存在: " + (token != null ? "是" : "否"));
        if (token != null) {
            System.out.println("Token内容: " + token.substring(0, Math.min(20, token.length())) + "...");
        }
        
        if (token != null) {
            try {
                String username = jwtUtil.extractUsername(token);
                System.out.println("从Token提取的用户名: " + username);
                
                Optional<User> userOpt = userService.findByUsername(username);
                System.out.println("数据库查询结果: " + (userOpt.isPresent() ? "找到用户" : "未找到用户"));
                
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    System.out.println("找到用户详情: ID=" + user.getUserId() + ", Username=" + user.getUsername() + ", UserGroup=" + user.getUserGroup());
                    return user;
                } else {
                    System.out.println("未找到用户: " + username);
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Token解析失败: " + e.getMessage());
                return null;
            }
        }
        System.out.println("没有Token，返回null");
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
     * 创建新文章
     * @param request 文章创建请求体
     * @return 创建成功的文章信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ArticleDto>> createArticle(@Validated @RequestBody ArticleCreateRequest request) {
        try {
            ArticleDto article = articleService.createArticle(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(article, "文章创建成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("创建文章失败: " + e.getMessage()));
        }
    }

    /**
     * 检查文章标题是否已存在
     * @param title 要检查的标题
     * @return 检查结果
     */
    @GetMapping("/check-title")
    public ResponseEntity<ApiResponse<Boolean>> checkTitleExists(@RequestParam String title) {
        try {
            boolean exists = articleService.titleExists(title);
            return ResponseEntity.ok(ApiResponse.success(exists, exists ? "标题已存在" : "标题可用"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("检查标题失败: " + e.getMessage()));
        }
    }

    /**
     * 分页获取所有文章
     * @param page 页码（默认0）
     * @param size 每页数量（默认10）
     * @param sortBy 排序字段（默认publishDate）
     * @param sortDir 排序方向（默认desc）
     * @return 文章分页数据
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ArticleDto>>> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<ArticleDto> articles = articleService.getAllArticles(page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success(articles, "获取文章列表成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取文章列表失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取文章详情（并自增浏览量）
     * @param id 文章ID
     * @return 文章详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleDto>> getArticleById(@PathVariable Long id) {
        try {
            ArticleDto article = articleService.getArticleByIdAndIncrementViews(id);
            return ResponseEntity.ok(ApiResponse.success(article, "获取文章成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取文章失败: " + e.getMessage()));
        }
    }

    /**
     * 更新文章内容
     * @param id 文章ID
     * @param request 文章更新请求体
     * @return 更新后的文章信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleDto>> updateArticle(
            @PathVariable Long id,
            @Validated @RequestBody ArticleUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 获取当前编辑用户
            User currentUser = getCurrentUser(httpRequest);
            String editorUsername = (currentUser != null) ? currentUser.getUsername() : "匿名用户";
            
            System.out.println("=== updateArticle Debug ===");
            System.out.println("文章ID: " + id);
            System.out.println("当前编辑用户: " + (currentUser != null ? currentUser.getUsername() : "null"));
            System.out.println("编辑者用户名: " + editorUsername);
            
            ArticleDto article = articleService.updateArticle(id, request, editorUsername);
            return ResponseEntity.ok(ApiResponse.success(article, "文章更新成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("更新文章失败: " + e.getMessage()));
        }
    }

    /**
     * 测试API：手动创建版本记录来验证编辑者记录功能
     * @param id 文章ID
     * @param request HTTP请求
     * @return 测试结果
     */
    @PostMapping("/{id}/test-version")
    public ResponseEntity<ApiResponse<String>> testCreateVersion(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            // 获取当前用户
            User currentUser = getCurrentUser(request);
            String editorUsername = (currentUser != null) ? currentUser.getUsername() : "测试用户";
            
            System.out.println("=== 测试版本创建 ===");
            System.out.println("文章ID: " + id);
            System.out.println("测试编辑者: " + editorUsername);
            
            // 创建一个测试版本
            articleVersionService.createVersion(id, "测试版本内容 - " + System.currentTimeMillis(), editorUsername);
            
            return ResponseEntity.ok(ApiResponse.success("测试版本创建成功，编辑者: " + editorUsername, "版本创建测试完成"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("测试版本创建失败: " + e.getMessage()));
        }
    }

    /**
     * 调试API：检查当前认证状态
     * @param request HTTP请求
     * @return 认证状态信息
     */
    @GetMapping("/debug/auth-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugAuthStatus(HttpServletRequest request) {
        Map<String, Object> debugInfo = new HashMap<>();
        
        try {
            // 检查Authorization头部
            String authHeader = request.getHeader("Authorization");
            debugInfo.put("authorizationHeader", authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null");
            
            // 提取token
            String token = extractTokenFromRequest(request);
            debugInfo.put("tokenExtracted", token != null);
            
            if (token != null) {
                // 解析用户名
                try {
                    String username = jwtUtil.extractUsername(token);
                    debugInfo.put("usernameFromToken", username);
                    
                    // 查找用户
                    Optional<User> userOpt = userService.findByUsername(username);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        debugInfo.put("userFound", true);
                        debugInfo.put("userId", user.getUserId());
                        debugInfo.put("username", user.getUsername());
                        debugInfo.put("userGroup", user.getUserGroup());
                        debugInfo.put("active", user.getActive());
                    } else {
                        debugInfo.put("userFound", false);
                        debugInfo.put("userSearchedFor", username);
                    }
                } catch (Exception e) {
                    debugInfo.put("tokenParseError", e.getMessage());
                }
            } else {
                debugInfo.put("tokenExtracted", false);
            }
            
            return ResponseEntity.ok(ApiResponse.success(debugInfo, "认证状态调试信息"));
        } catch (Exception e) {
            debugInfo.put("generalError", e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(debugInfo, "认证状态调试信息（有错误）"));
        }
    }

    /**
     * 删除文章
     * @param id 文章ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        try {
            articleService.deleteArticle(id);
            return ResponseEntity.ok(ApiResponse.success(null, "文章删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("删除文章失败: " + e.getMessage()));
        }
    }

    /**
     * 根据分类分页获取文章
     * @param category 分类名
     * @param page 页码
     * @param size 每页数量
     * @return 分类下的文章分页数据
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<ArticleDto>>> getArticlesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ArticleDto> articles = articleService.getArticlesByCategory(category, page, size);
            return ResponseEntity.ok(ApiResponse.success(articles, "获取分类文章成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取分类文章失败: " + e.getMessage()));
        }
    }

    /**
     * 根据作者获取文章列表
     * @param author 作者名
     * @return 作者的所有文章
     */
    @GetMapping("/author/{author}")
    public ResponseEntity<ApiResponse<List<ArticleDto>>> getArticlesByAuthor(@PathVariable String author) {
        try {
            List<ArticleDto> articles = articleService.getArticlesByAuthor(author);
            return ResponseEntity.ok(ApiResponse.success(articles, "获取作者文章成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取作者文章失败: " + e.getMessage()));
        }
    }

    /**
     * 搜索文章
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @return 搜索结果分页数据
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ArticleDto>>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ArticleDto> articles = articleService.searchArticles(keyword, page, size);
            return ResponseEntity.ok(ApiResponse.success(articles, "搜索文章成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("搜索文章失败: " + e.getMessage()));
        }
    }

    /**
     * 根据标签分页获取文章
     * @param tag 标签名
     * @param page 页码
     * @param size 每页数量
     * @return 标签下的文章分页数据
     */
    @GetMapping("/tag/{tag}")
    public ResponseEntity<ApiResponse<Page<ArticleDto>>> getArticlesByTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ArticleDto> articles = articleService.getArticlesByTag(tag, page, size);
            return ResponseEntity.ok(ApiResponse.success(articles, "获取标签文章成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取标签文章失败: " + e.getMessage()));
        }
    }

    /**
     * 获取热门文章
     * @param limit 返回数量
     * @return 热门文章列表
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ArticleDto>>> getPopularArticles(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ArticleDto> articles = articleService.getPopularArticles(limit);
            return ResponseEntity.ok(ApiResponse.success(articles, "获取热门文章成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取热门文章失败: " + e.getMessage()));
        }
    }

    /**
     * 获取最新文章
     * @param limit 返回数量
     * @return 最新文章列表
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<ArticleDto>>> getLatestArticles(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ArticleDto> articles = articleService.getLatestArticles(limit);
            return ResponseEntity.ok(ApiResponse.success(articles, "获取最新文章成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取最新文章失败: " + e.getMessage()));
        }
    }

    /**
     * 点赞文章
     * @param id 文章ID
     * @return 点赞结果
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> likeArticle(@PathVariable Long id) {
        try {
            articleService.likeArticle(id);
            return ResponseEntity.ok(ApiResponse.success(null, "点赞成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("点赞失败: " + e.getMessage()));
        }
    }

    /**
     * 取消点赞
     * @param id 文章ID
     * @return 取消点赞结果
     */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> unlikeArticle(@PathVariable Long id) {
        try {
            articleService.unlikeArticle(id);
            return ResponseEntity.ok(ApiResponse.success(null, "取消点赞成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("取消点赞失败: " + e.getMessage()));
        }
    }

    /**
     * 更新评论数
     * @param id 文章ID
     * @param count 新评论数
     * @return 更新结果
     */
    @PutMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<Void>> updateCommentsCount(
            @PathVariable Long id,
            @RequestParam Integer count) {
        try {
            articleService.updateCommentsCount(id, count);
            return ResponseEntity.ok(ApiResponse.success(null, "评论数更新成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("评论数更新失败: " + e.getMessage()));
        }
    }

    /**
     * 获取文章统计信息
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ArticleStatisticsDto>> getStatistics() {
        try {
            ArticleStatisticsDto statistics = articleService.getStatistics();
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取统计信息成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }

    /**
     * 根据标题获取文章ID
     * @param title 文章标题
     * @return 文章ID
     */
    @GetMapping("/title/{title}/id")
    public ResponseEntity<ApiResponse<Long>> getArticleIdByTitle(@PathVariable String title) {
        try {
            Long articleId = articleService.getArticleIdByTitle(title);
            if (articleId != null) {
                return ResponseEntity.ok(ApiResponse.success(articleId, "获取文章ID成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("未找到标题为 '" + title + "' 的文章"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取文章ID失败: " + e.getMessage()));
        }
    }

    /**
     * 获取文章贡献者列表
     * @param id 文章ID
     * @return 贡献者列表
     */
    @GetMapping("/{id}/contributors")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getArticleContributors(@PathVariable Long id) {
        try {
            List<Map<String, Object>> contributors = articleService.getArticleContributors(id);
            return ResponseEntity.ok(ApiResponse.success(contributors, "获取贡献者成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取贡献者失败: " + e.getMessage()));
        }
    }
}
