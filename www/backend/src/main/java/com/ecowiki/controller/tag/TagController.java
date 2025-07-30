package com.ecowiki.controller.tag;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.dto.tag.TagDto;
import com.ecowiki.dto.tag.TagStatisticsDto;
import com.ecowiki.service.TagService;

/**
 * 标签控制器
 * 
 * 提供标签相关的REST API接口，包括标签的增删改查、搜索、统计等功能。
 * 支持标签管理、热门标签查询、推荐标签等业务场景。
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/tags")
public class TagController {
    
    @Autowired
    private TagService tagService;
    
    /**
     * 创建新标签
     * 
     * @param tagName 标签名称（必填）
     * @param description 标签描述（可选）
     * @return 创建的标签信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TagDto>> createTag(
            @RequestParam String tagName,
            @RequestParam(required = false) String description) {
        try {
            TagDto tag = tagService.createTag(tagName, description);
            return ResponseEntity.ok(ApiResponse.success(tag, "标签创建成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("标签创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取标签
     * 
     * @param tagId 标签ID
     * @return 标签信息
     */
    @GetMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagDto>> getTagById(@PathVariable Long tagId) {
        try {
            TagDto tag = tagService.getTagById(tagId);
            return ResponseEntity.ok(ApiResponse.success(tag, "获取标签成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取标签失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据名称获取标签
     * 
     * @param tagName 标签名称
     * @return 标签信息
     */
    @GetMapping("/name/{tagName}")
    public ResponseEntity<ApiResponse<TagDto>> getTagByName(@PathVariable String tagName) {
        try {
            TagDto tag = tagService.getTagByName(tagName);
            return ResponseEntity.ok(ApiResponse.success(tag, "获取标签成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取标签失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新标签信息
     * 
     * @param tagId 标签ID
     * @param tagName 新的标签名称
     * @param description 新的标签描述
     * @return 更新后的标签信息
     */
    @PutMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagDto>> updateTag(
            @PathVariable Long tagId,
            @RequestParam String tagName,
            @RequestParam(required = false) String description) {
        try {
            TagDto tag = tagService.updateTag(tagId, tagName, description);
            return ResponseEntity.ok(ApiResponse.success(tag, "标签更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("标签更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除标签
     * 
     * @param tagId 标签ID
     * @return 删除结果
     */
    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponse<String>> deleteTag(@PathVariable Long tagId) {
        try {
            tagService.deleteTag(tagId);
            return ResponseEntity.ok(ApiResponse.success(null, "标签删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("标签删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有标签
     * 
     * @return 所有标签列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TagDto>>> getAllTags() {
        try {
            List<TagDto> tags = tagService.getAllTags();
            return ResponseEntity.ok(ApiResponse.success(tags, "获取标签列表成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取标签列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索标签
     * 
     * @param keyword 搜索关键词
     * @param page 页码（默认0）
     * @param size 每页数量（默认10）
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TagDto>>> searchTags(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TagDto> tags = tagService.searchTags(keyword, page, size);
            return ResponseEntity.ok(ApiResponse.success(tags, "搜索标签成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("搜索标签失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取热门标签
     * 
     * @param limit 返回数量限制（默认10）
     * @return 热门标签列表
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<TagDto>>> getPopularTags(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<TagDto> tags = tagService.getPopularTags(limit);
            return ResponseEntity.ok(ApiResponse.success(tags, "获取热门标签成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取热门标签失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取最近创建的标签
     * 
     * @param limit 返回数量限制（默认10）
     * @return 最近创建的标签列表
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<TagDto>>> getRecentTags(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<TagDto> tags = tagService.getRecentTags(limit);
            return ResponseEntity.ok(ApiResponse.success(tags, "获取最新标签成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取最新标签失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取标签统计信息
     * 
     * @return 标签统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<TagStatisticsDto>> getTagStatistics() {
        try {
            TagStatisticsDto statistics = tagService.getTagStatistics();
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取标签统计成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取标签统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取推荐标签
     * 
     * @param articleId 文章ID
     * @param limit 返回数量限制（默认5）
     * @return 推荐标签列表
     */
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<TagDto>>> getRecommendedTags(
            @RequestParam Long articleId,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<TagDto> tags = tagService.getRecommendedTags(articleId, limit);
            return ResponseEntity.ok(ApiResponse.success(tags, "获取推荐标签成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取推荐标签失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除未使用的标签
     * 
     * @return 删除的标签数量
     */
    @DeleteMapping("/unused")
    public ResponseEntity<ApiResponse<Integer>> deleteUnusedTags() {
        try {
            int deletedCount = tagService.deleteUnusedTags();
            return ResponseEntity.ok(ApiResponse.success(deletedCount, 
                    "删除未使用标签成功，共删除 " + deletedCount + " 个标签"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除未使用标签失败: " + e.getMessage()));
        }
    }
}
