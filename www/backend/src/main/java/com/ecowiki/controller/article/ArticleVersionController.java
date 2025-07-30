package com.ecowiki.controller.article;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.entity.article.ArticleVersion;
import com.ecowiki.entity.article.ArticleVersionStats;
import com.ecowiki.service.ArticleVersionService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
/**
 * 文章版本控制器
 * 提供文章版本管理的REST API
 * 
 * @author EcoWiki
 * @version 1.0
 */
@RestController
@RequestMapping("/articles/{articleId}/versions")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ArticleVersionController {
    
    @Autowired
    private ArticleVersionService versionService;
    
    /**
     * 创建文章新版本
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createVersion(
            @PathVariable @NotNull @Min(1) Long articleId,
            @RequestBody CreateVersionRequest request) {
        
        try {
            ArticleVersion version = versionService.createVersion(
                    articleId, 
                    request.getContent(), 
                    request.getAuthor()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("versionId", version.getVersionId());
            response.put("versionNumber", version.getVersionNumber());
            response.put("storageType", version.getStorageType());
            response.put("createdAt", version.getCreatedAt());
            response.put("contentHash", version.getContentHash());
            
            if (version.getOriginalSize() != null && version.getCompressedSize() != null) {
                response.put("compressionRatio", version.getCompressionRatio());
                response.put("originalSize", version.getOriginalSize());
                response.put("compressedSize", version.getCompressedSize());
            }
            
            return ResponseEntity.ok(ApiResponse.success(response, "创建版本成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("创建版本失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取指定版本的内容
     */
    @GetMapping("/{versionNumber}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVersionContent(
            @PathVariable @NotNull @Min(1) Long articleId,
            @PathVariable @NotNull @Min(1) Integer versionNumber) {
        
        try {
            String content = versionService.getVersionContent(articleId, versionNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", content);
            response.put("versionNumber", versionNumber);
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取版本内容成功"));
            
        } catch (Exception e) {
            System.err.println("Error getting version content for articleId=" + articleId + 
                             ", versionNumber=" + versionNumber + ": " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("获取版本内容失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取最新版本的内容
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLatestVersionContent(
            @PathVariable @NotNull @Min(1) Long articleId) {
        
        try {
            String content = versionService.getLatestVersionContent(articleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", content);
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取最新版本内容成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取最新版本内容失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取版本历史列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVersionHistory(
            @PathVariable @NotNull @Min(1) Long articleId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ArticleVersion> versionPage = versionService.getVersionHistory(articleId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("versions", versionPage.getContent().stream().map(this::convertToVersionSummary).collect(Collectors.toList()));
            response.put("totalElements", versionPage.getTotalElements());
            response.put("totalPages", versionPage.getTotalPages());
            response.put("currentPage", page);
            response.put("size", size);
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取版本历史成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取版本历史失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取版本统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVersionStats(
            @PathVariable @NotNull @Min(1) Long articleId) {
        
        try {
            ArticleVersionStats stats = versionService.getVersionStats(articleId);
            
            Map<String, Object> response = convertToStatsResponse(stats);
            
            return ResponseEntity.ok(ApiResponse.success(response, "获取版本统计成功"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取版本统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 恢复文章到指定版本
     */
    @PostMapping("/{versionNumber}/restore")
    public ResponseEntity<ApiResponse<Map<String, Object>>> restoreToVersion(
            @PathVariable @NotNull @Min(1) Long articleId,
            @PathVariable @NotNull @Min(1) Integer versionNumber,
            @RequestBody RestoreVersionRequest request) {
        
        try {
            System.out.println("开始恢复版本 - articleId: " + articleId + ", versionNumber: " + versionNumber + ", author: " + request.getAuthor());
            
            ArticleVersion restoredVersion = versionService.restoreToVersion(
                    articleId, versionNumber, request.getAuthor());
            
            System.out.println("版本恢复成功 - 新版本ID: " + restoredVersion.getVersionId() + ", 新版本号: " + restoredVersion.getVersionNumber());
            
            Map<String, Object> response = new HashMap<>();
            response.put("newVersionId", restoredVersion.getVersionId());
            response.put("newVersionNumber", restoredVersion.getVersionNumber());
            response.put("restoredFromVersion", versionNumber);
            
            return ResponseEntity.ok(ApiResponse.success(response, "版本恢复成功"));
            
        } catch (Exception e) {
            System.err.println("版本恢复失败 - articleId: " + articleId + ", versionNumber: " + versionNumber + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("版本恢复失败: " + e.getMessage()));
        }
    }

    /**
     * 转换版本为摘要信息
     */
    private Map<String, Object> convertToVersionSummary(ArticleVersion version) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("versionId", version.getVersionId());
        summary.put("versionNumber", version.getVersionNumber());
        summary.put("author", version.getAuthor());
        summary.put("createdAt", version.getCreatedAt());
        summary.put("storageType", version.getStorageType());
        summary.put("isArchived", version.getIsArchived());
        summary.put("changeSummary", version.getChangeSummary());
        
        if (version.getOriginalSize() != null && version.getCompressedSize() != null) {
            summary.put("originalSize", version.getOriginalSize());
            summary.put("compressedSize", version.getCompressedSize());
            summary.put("compressionRatio", version.getCompressionRatio());
        }
        
        return summary;
    }
    
    /**
     * 转换统计信息为响应格式
     */
    private Map<String, Object> convertToStatsResponse(ArticleVersionStats stats) {
        Map<String, Object> response = new HashMap<>();
        response.put("totalVersions", stats.getTotalVersions());
        response.put("baseVersionsCount", stats.getBaseVersionsCount());
        response.put("diffVersionsCount", stats.getDiffVersionsCount());
        response.put("archivedVersionsCount", stats.getArchivedVersionsCount());
        response.put("totalStorageSize", stats.getTotalStorageSize());
        response.put("compressedStorageSize", stats.getCompressedStorageSize());
        response.put("compressionRatio", stats.getCompressionRatio());
        response.put("accessFrequency", stats.getAccessFrequency());
        response.put("lastAccessedAt", stats.getLastAccessedAt());
        response.put("lastOptimizedAt", stats.getLastOptimizedAt());
        response.put("optimizationNeeded", stats.getOptimizationNeeded());
        response.put("needsOptimization", stats.needsOptimization());
        
        return response;
    }
    
    /**
     * 创建版本请求DTO
     */
    public static class CreateVersionRequest {
        @NotBlank(message = "Content cannot be blank")
        private String content;
        
        @NotBlank(message = "Author cannot be blank")
        private String author;
        
        private String changeSummary;
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public String getAuthor() {
            return author;
        }
        
        public void setAuthor(String author) {
            this.author = author;
        }
        
        public String getChangeSummary() {
            return changeSummary;
        }
        
        public void setChangeSummary(String changeSummary) {
            this.changeSummary = changeSummary;
        }
    }

    /**
     * 恢复版本请求DTO
     */
    public static class RestoreVersionRequest {
        @NotBlank(message = "Author cannot be blank")
        private String author;
        
        public String getAuthor() {
            return author;
        }
        
        public void setAuthor(String author) {
            this.author = author;
        }
    }
}
