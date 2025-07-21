package com.ecowiki.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * 评论实体类
 * 
 * 对应数据库中的comments表
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2025-01-21
 */
@Entity
@Table(name = "comments")
public class Comment {
    
    /**
     * 评论ID，主键，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;
    
    /**
     * 文章ID，关联到articles表
     */
    @Column(name = "article_id")
    private Long articleId;
    
    /**
     * 用户ID，关联到user表
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 父评论ID，用于实现回复功能
     */
    @Column(name = "parent_comment_id")
    private Long parentCommentId;
    
    /**
     * 评论内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    /**
     * 评论时间
     */
    @Column(name = "comment_time")
    private LocalDateTime commentTime;
    
    /**
     * 点赞数
     */
    @Column(name = "likes")
    private Integer likes = 0;
    
    /**
     * 是否已删除（软删除）
     */
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    /**
     * 一对多关系：一个评论可以有多个回复
     */
    @OneToMany(mappedBy = "parentCommentId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> replies;
    
    // 默认构造函数
    public Comment() {
        this.commentTime = LocalDateTime.now();
    }
    
    // 带参数的构造函数
    public Comment(Long articleId, Long userId, String content) {
        this();
        this.articleId = articleId;
        this.userId = userId;
        this.content = content;
    }
    
    // Getter和Setter方法
    public Long getCommentId() {
        return commentId;
    }
    
    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }
    
    public Long getArticleId() {
        return articleId;
    }
    
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getParentCommentId() {
        return parentCommentId;
    }
    
    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getCommentTime() {
        return commentTime;
    }
    
    public void setCommentTime(LocalDateTime commentTime) {
        this.commentTime = commentTime;
    }
    
    public Integer getLikes() {
        return likes;
    }
    
    public void setLikes(Integer likes) {
        this.likes = likes;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    
    public List<Comment> getReplies() {
        return replies;
    }
    
    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }
    
    /**
     * 增加点赞数
     */
    public void incrementLikes() {
        if (this.likes == null) {
            this.likes = 1;
        } else {
            this.likes = this.likes + 1;
        }
    }
    
    /**
     * 减少点赞数
     */
    public void decrementLikes() {
        if (this.likes == null || this.likes <= 0) {
            this.likes = 0;
        } else {
            this.likes = this.likes - 1;
        }
    }
    
    /**
     * 软删除评论
     */
    public void softDelete() {
        this.deleted = true;
        this.content = "[此评论已删除]";
    }
    
    /**
     * 判断是否为顶级评论
     */
    public boolean isTopLevel() {
        return this.parentCommentId == null;
    }
    
    @Override
    public String toString() {
        return "Comment{" +
                "commentId=" + commentId +
                ", articleId=" + articleId +
                ", userId=" + userId +
                ", parentCommentId=" + parentCommentId +
                ", content='" + content + '\'' +
                ", commentTime=" + commentTime +
                ", likes=" + likes +
                ", deleted=" + deleted +
                '}';
    }
}
