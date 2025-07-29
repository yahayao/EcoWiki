package com.ecowiki.dto.comment;

import java.util.List;

/**
 * 评论DTO类
 * 
 * 用于前端交互的评论数据传输对象
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2025-01-21
 */
public class CommentDTO {
    private String id;
    private Long articleId;
    private String author;
    private String userAvatar;  // 用户头像URL
    private String content;
    private String createdAt;
    private Integer likes;
    private Boolean isLiked;
    private List<ReplyDTO> replies;
    
    public CommentDTO() {}
    public CommentDTO(String id, Long articleId, String author, String content, String createdAt, Integer likes, Boolean isLiked) {
        this.id = id;
        this.articleId = articleId;
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
        this.likes = likes;
        this.isLiked = isLiked;
    }

    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Long getArticleId() {
        return articleId;
    }
    
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getUserAvatar() {
        return userAvatar;
    }
    
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public Integer getLikes() {
        return likes;
    }
    
    public void setLikes(Integer likes) {
        this.likes = likes;
    }
    
    public Boolean getIsLiked() {
        return isLiked;
    }
    
    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }
    
    public List<ReplyDTO> getReplies() {
        return replies;
    }
    
    public void setReplies(List<ReplyDTO> replies) {
        this.replies = replies;
    }
}
