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

@RestController
@RequestMapping("/articles")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class ArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    // 创建文章
    @PostMapping
    public ResponseEntity<ApiResponse<ArticleDto>> createArticle(@Validated @RequestBody ArticleCreateRequest request) {
        try {
            ArticleDto article = articleService.createArticle(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(article, "文章创建成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("创建文章失败: " + e.getMessage()));
        }
    }
    
    // 获取所有文章（分页）
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
    
    // 根据ID获取文章
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
    
    // 更新文章
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
    
    // 删除文章
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
    
    // 根据分类获取文章
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
    
    // 根据作者获取文章
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
    
    // 搜索文章
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
    
    // 根据标签获取文章
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
    
    // 获取热门文章
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
    
    // 获取最新文章
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
    
    // 点赞文章
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
    
    // 取消点赞
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
    
    // 更新评论数
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
    
    // 获取文章统计信息
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
}
