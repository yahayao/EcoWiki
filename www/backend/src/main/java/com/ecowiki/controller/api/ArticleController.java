package com.ecowiki.controller.api;

import java.util.List;

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
import com.ecowiki.dto.ArticleCreateRequest;
import com.ecowiki.dto.ArticleDto;
import com.ecowiki.dto.ArticleStatisticsDto;
import com.ecowiki.dto.ArticleUpdateRequest;
import com.ecowiki.service.ArticleService;

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
            @Validated @RequestBody ArticleUpdateRequest request) {
        try {
            ArticleDto article = articleService.updateArticle(id, request);
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
}
