package com.ecowiki.controller.article;

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
import com.ecowiki.entity.user.User;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.ArticleDraftService;
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
     * 文章草稿服务，用于处理文章审核流程
     */
    @Autowired
    private ArticleDraftService articleDraftService;

    /**
     * 获取当前请求用户实体
     * @param request HTTP请求
     * @return 当前用户对象，若无效则为null
     */
    private User getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        
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
     * 创建新文章（提交到草稿表等待审核）
     * @param request 文章创建请求体
     * @param httpRequest HTTP请求
     * @return 创建的草稿信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> createArticle(
            @Validated @RequestBody ArticleCreateRequest request,
            HttpServletRequest httpRequest) {
        
        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "请先登录"));
        }
        
        try {
            // 提交新文章到草稿表等待审核
            var draft = articleDraftService.submitNewArticleDraft(request, currentUser.getUserId());
            
            // 返回提示信息，告知用户文章已提交审核
            Map<String, Object> result = new HashMap<>();
            result.put("draftId", draft.getDraftId());
            result.put("status", "submitted_for_review");
            result.put("message", "新文章已提交，等待管理员审核");
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "文章已提交审核，请耐心等待"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("提交文章失败: " + e.getMessage()));
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
     * 更新文章内容（提交到草稿表等待审核）
     * @param id 文章ID
     * @param request 文章更新请求体
     * @param httpRequest HTTP请求
     * @return 提交的草稿信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> updateArticle(
            @PathVariable Long id,
            @Validated @RequestBody ArticleUpdateRequest request,
            HttpServletRequest httpRequest) {
        
        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "请先登录"));
        }
        
        try {
            // 提交文章编辑到草稿表等待审核
            var draft = articleDraftService.submitArticleEditDraft(id, request, currentUser.getUserId());
            
            // 返回提示信息，告知用户文章已提交审核
            Map<String, Object> result = new HashMap<>();
            result.put("draftId", draft.getDraftId());
            result.put("status", "submitted_for_review");
            result.put("message", "文章修改已提交，等待管理员审核");
            
            return ResponseEntity.ok(ApiResponse.success(result, "文章修改已提交审核，请耐心等待"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "提交文章修改失败: " + e.getMessage()));
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
     * 为文章点赞
     * <p>
     * 用户为指定文章添加点赞，支持用户对喜欢内容的互动表达。
     * 同一用户对同一文章只能点赞一次，重复点赞会被忽略。
     * 
     * @param id 文章ID
     * @param request HTTP请求对象，用于获取当前用户信息
     * @return ResponseEntity 包含操作结果的响应
     * @apiNote 需要用户登录，未登录返回401状态码
     * @since 1.0
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> likeArticle(@PathVariable Long id, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("请先登录"));
            }
            
            articleService.likeArticle(id, currentUser.getUserId());
            return ResponseEntity.ok(ApiResponse.success(null, "点赞成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("点赞失败: " + e.getMessage()));
        }
    }

    /**
     * 取消文章点赞
     * <p>
     * 用户取消对指定文章的点赞，支持用户撤回之前的互动操作。
     * 如果用户之前未点赞该文章，此操作不会产生任何影响。
     * 
     * @param id 文章ID
     * @param request HTTP请求对象，用于获取当前用户信息
     * @return ResponseEntity 包含操作结果的响应
     * @apiNote 需要用户登录，未登录返回401状态码
     * @since 1.0
     */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> unlikeArticle(@PathVariable Long id, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("请先登录"));
            }
            
            articleService.unlikeArticle(id, currentUser.getUserId());
            return ResponseEntity.ok(ApiResponse.success(null, "取消点赞成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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
    
    // ========== 收藏相关API ==========
    
    /**
     * 收藏文章
     * <p>
     * 用户将指定文章添加到个人收藏列表，便于后续快速访问。
     * 收藏的文章可以在用户个人中心查看，支持个人内容管理功能。
     * 
     * @param id 文章ID
     * @param request HTTP请求对象，用于获取当前用户信息
     * @return ResponseEntity 包含操作结果的响应
     * @apiNote 需要用户登录，未登录返回401状态码
     * @since 1.0
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<Void>> favoriteArticle(@PathVariable Long id, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("请先登录"));
            }
            
            articleService.favoriteArticle(id, currentUser.getUserId());
            return ResponseEntity.ok(ApiResponse.success(null, "收藏成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("收藏失败: " + e.getMessage()));
        }
    }

    /**
     * 取消收藏文章
     * <p>
     * 用户从个人收藏列表中移除指定文章，撤回之前的收藏操作。
     * 如果用户之前未收藏该文章，此操作不会产生任何影响。
     * 
     * @param id 文章ID
     * @param request HTTP请求对象，用于获取当前用户信息
     * @return ResponseEntity 包含操作结果的响应
     * @apiNote 需要用户登录，未登录返回401状态码
     * @since 1.0
     */
    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<Void>> unfavoriteArticle(@PathVariable Long id, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("请先登录"));
            }
            
            articleService.unfavoriteArticle(id, currentUser.getUserId());
            return ResponseEntity.ok(ApiResponse.success(null, "取消收藏成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("取消收藏失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查文章点赞状态
     * @param id 文章ID
     * @param request HTTP请求
     * @return 点赞状态
     */
    @GetMapping("/{id}/like-status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkLikeStatus(@PathVariable Long id, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            boolean liked = false;
            
            if (currentUser != null) {
                liked = articleService.isArticleLikedByUser(id, currentUser.getUserId());
            }
            
            Map<String, Boolean> result = new HashMap<>();
            result.put("liked", liked);
            
            return ResponseEntity.ok(ApiResponse.success(result, "获取点赞状态成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取点赞状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查文章收藏状态
     * @param id 文章ID
     * @param request HTTP请求
     * @return 收藏状态
     */
    @GetMapping("/{id}/favorite-status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFavoriteStatus(@PathVariable Long id, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            boolean favorited = false;
            
            if (currentUser != null) {
                favorited = articleService.isArticleFavoritedByUser(id, currentUser.getUserId());
            }
            
            Map<String, Boolean> result = new HashMap<>();
            result.put("favorited", favorited);
            
            return ResponseEntity.ok(ApiResponse.success(result, "获取收藏状态成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取收藏状态失败: " + e.getMessage()));
        }
    }
}
