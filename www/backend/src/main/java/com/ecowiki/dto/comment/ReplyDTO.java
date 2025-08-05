/**
 * 回复DTO类
 * 
 * 功能：
 * - 用于前端交互的回复数据传输对象
 * - 包含回复的基本信息和状态
 * - 支持用户头像和点赞状态显示
 * - 提供完整的回复信息结构
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.dto.comment;

public class ReplyDTO {
    private String id;
    private String commentId;
    private String author;
    private String userAvatar;  // 用户头像URL
    private String content;
    private String createdAt;
    private Integer likes;
    private Boolean isLiked;
    public ReplyDTO() {}
    public ReplyDTO(String id, String commentId, String author, String content, String createdAt, Integer likes, Boolean isLiked) {
        this.id = id;
        this.commentId = commentId;
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
    
    public String getCommentId() {
        return commentId;
    }
    
    public void setCommentId(String commentId) {
        this.commentId = commentId;
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
}
