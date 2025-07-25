package com.ecowiki.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * 文章审核实体类
 * 
 * 管理文章的审核流程，包括创建、更新、删除等操作的审核
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-07-25
 */
@Entity
@Table(name = "article_review")
public class ArticleReview {
    
    /**
     * 审核ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;
    
    /**
     * 文章ID
     */
    @NotNull(message = "文章ID不能为空")
    @Column(name = "article_id", nullable = false)
    private Long articleId;
    
    /**
     * 审核员ID
     */
    @Column(name = "reviewer_id")
    private Long reviewerId;
    
    /**
     * 提交人ID
     */
    @NotNull(message = "提交人ID不能为空")
    @Column(name = "submitter_id", nullable = false)
    private Long submitterId;
    
    /**
     * 审核类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;
    
    /**
     * 审核状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;
    
    /**
     * 内容快照
     */
    @Lob
    @Column(name = "content_snapshot", columnDefinition = "LONGTEXT")
    private String contentSnapshot;
    
    /**
     * 审核原因/备注
     */
    @Column(name = "review_reason", columnDefinition = "TEXT")
    private String reviewReason;
    
    /**
     * 提交时间
     */
    @Column(name = "submit_time")
    private LocalDateTime submitTime;
    
    /**
     * 审核时间
     */
    @Column(name = "review_time")
    private LocalDateTime reviewTime;
    
    /**
     * 是否自动发布
     */
    @Column(name = "auto_publish")
    private Boolean autoPublish = false;
    
    /**
     * 优先级（1-5）
     */
    @Column(name = "priority_level")
    private Integer priorityLevel = 1;
    
    /**
     * 审核截止时间
     */
    @Column(name = "review_deadline")
    private LocalDateTime reviewDeadline;
    
    /**
     * 审核类型枚举
     */
    public enum ReviewType {
        CREATE("创建文章"),
        UPDATE("更新文章"),
        DELETE("删除文章");
        
        private final String description;
        
        ReviewType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 审核状态枚举
     */
    public enum ReviewStatus {
        PENDING("待审核"),
        APPROVED("审核通过"),
        REJECTED("审核拒绝"),
        CANCELLED("已取消");
        
        private final String description;
        
        ReviewStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 构造函数
    public ArticleReview() {
        this.submitTime = LocalDateTime.now();
    }
    
    public ArticleReview(Long articleId, Long submitterId, ReviewType reviewType) {
        this();
        this.articleId = articleId;
        this.submitterId = submitterId;
        this.reviewType = reviewType;
    }
    
    // Getters and Setters
    public Long getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }
    
    public Long getArticleId() {
        return articleId;
    }
    
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    
    public Long getReviewerId() {
        return reviewerId;
    }
    
    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }
    
    public Long getSubmitterId() {
        return submitterId;
    }
    
    public void setSubmitterId(Long submitterId) {
        this.submitterId = submitterId;
    }
    
    public ReviewType getReviewType() {
        return reviewType;
    }
    
    public void setReviewType(ReviewType reviewType) {
        this.reviewType = reviewType;
    }
    
    public ReviewStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReviewStatus status) {
        this.status = status;
    }
    
    public String getContentSnapshot() {
        return contentSnapshot;
    }
    
    public void setContentSnapshot(String contentSnapshot) {
        this.contentSnapshot = contentSnapshot;
    }
    
    public String getReviewReason() {
        return reviewReason;
    }
    
    public void setReviewReason(String reviewReason) {
        this.reviewReason = reviewReason;
    }
    
    public LocalDateTime getSubmitTime() {
        return submitTime;
    }
    
    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }
    
    public LocalDateTime getReviewTime() {
        return reviewTime;
    }
    
    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }
    
    public Boolean getAutoPublish() {
        return autoPublish;
    }
    
    public void setAutoPublish(Boolean autoPublish) {
        this.autoPublish = autoPublish;
    }
    
    public Integer getPriorityLevel() {
        return priorityLevel;
    }
    
    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }
    
    public LocalDateTime getReviewDeadline() {
        return reviewDeadline;
    }
    
    public void setReviewDeadline(LocalDateTime reviewDeadline) {
        this.reviewDeadline = reviewDeadline;
    }
    
    /**
     * 检查审核是否已过期
     */
    public boolean isOverdue() {
        return reviewDeadline != null && LocalDateTime.now().isAfter(reviewDeadline);
    }
    
    /**
     * 检查审核是否紧急（距离截止时间小于2小时）
     */
    public boolean isUrgent() {
        if (reviewDeadline == null) return false;
        return LocalDateTime.now().plusHours(2).isAfter(reviewDeadline);
    }
    
    /**
     * 获取审核耗时（小时）
     */
    public Long getReviewDurationHours() {
        if (reviewTime == null || submitTime == null) return null;
        return java.time.Duration.between(submitTime, reviewTime).toHours();
    }
    
    @Override
    public String toString() {
        return "ArticleReview{" +
                "reviewId=" + reviewId +
                ", articleId=" + articleId +
                ", reviewType=" + reviewType +
                ", status=" + status +
                ", submitTime=" + submitTime +
                ", reviewTime=" + reviewTime +
                '}';
    }
}
