package com.ecowiki.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 文章创建请求DTO
 * <p>
 * 用于接收前端创建文章的请求数据，包含文章的基本信息。
 * 支持数据验证，确保创建的文章数据完整性。
 * <p>
 * <b>设计说明：</b>
 * - 仅包含创建文章所需的必要字段
 * - 使用Bean Validation进行数据验证
 * - 不包含ID、时间戳等服务端生成的字段
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
public class ArticleCreateRequest {
    /** 文章标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;
    
    /** 文章作者 */
    @NotBlank(message = "作者不能为空")
    @Size(max = 50, message = "作者名称长度不能超过50个字符")
    private String author;
    
    /** 文章内容 */
    private String content;
    
    /** 文章分类 */
    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;
    
    /** 文章标签（多个标签以逗号分隔） */
    @Size(max = 255, message = "标签长度不能超过255个字符")
    private String tags;
    
    // 构造函数
    public ArticleCreateRequest() {}
    
    public ArticleCreateRequest(String title, String author, String content, String category) {
        this.title = title;
        this.author = author;
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
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
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
