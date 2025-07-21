package com.ecowiki.dto;

/**
 * 创建评论请求DTO
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2025-01-21
 */
public class CreateCommentRequest {
    
    private Long articleId;
    private String content;
    
    // 默认构造函数
    public CreateCommentRequest() {}
    
    // 带参数的构造函数
    public CreateCommentRequest(Long articleId, String content) {
        this.articleId = articleId;
        this.content = content;
    }
    
    // Getter和Setter方法
    public Long getArticleId() {
        return articleId;
    }
    
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}

/**
 * 创建回复请求DTO
 */
class CreateReplyRequest {
    
    private String content;
    
    // 默认构造函数
    public CreateReplyRequest() {}
    
    // 带参数的构造函数
    public CreateReplyRequest(String content) {
        this.content = content;
    }
    
    // Getter和Setter方法
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}

/**
 * 点赞结果DTO
 */
class LikeResult {
    
    private Boolean isLiked;
    private Integer likes;
    
    // 默认构造函数
    public LikeResult() {}
    
    // 带参数的构造函数
    public LikeResult(Boolean isLiked, Integer likes) {
        this.isLiked = isLiked;
        this.likes = likes;
    }
    
    // Getter和Setter方法
    public Boolean getIsLiked() {
        return isLiked;
    }
    
    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }
    
    public Integer getLikes() {
        return likes;
    }
    
    public void setLikes(Integer likes) {
        this.likes = likes;
    }
}
