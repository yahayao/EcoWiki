package com.ecowiki.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ArticleCreateRequest {
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;
    
    @NotBlank(message = "作者不能为空")
    @Size(max = 50, message = "作者名称长度不能超过50个字符")
    private String author;
    
    private String content;
    
    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;
    
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
