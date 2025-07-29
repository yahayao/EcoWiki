package com.ecowiki.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 文章更新请求DTO
 * <p>
 * 用于接收前端更新文章的请求数据，包含可修改的文章信息。
 * 支持数据验证，确保更新的文章数据完整性。
 * <p>
 * <b>设计说明：</b>
 * - 包含可更新的文章字段（标题、内容、分类、标签）
 * - 不包含作者字段（通常不允许修改）
 * - 使用Bean Validation进行数据验证
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
public class ArticleUpdateRequest {
    /** 文章标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;
    
    /** 文章内容 */
    private String content;
    
    /** 文章分类 */
    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;
    
    /** 文章标签（多个标签以逗号分隔） */
    @Size(max = 255, message = "标签长度不能超过255个字符")
    private String tags;
    
    // 构造函数
    public ArticleUpdateRequest() {}
    
    public ArticleUpdateRequest(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
}
