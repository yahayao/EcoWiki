package com.ecowiki.entity.article;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 文章草稿实体类
 * <p>
 * 对应数据库中的article_drafts表，存储待审核的文章草稿。
 * 用户编辑文章后会先保存到草稿表，等待管理员审核通过后才会发布到正式的articles表。
 * <p>
 * <b>审核流程：</b>
 * 1. 用户编辑文章 -> 保存到草稿表（状态：PENDING）
 * 2. 自动发送消息给superadmin
 * 3. superadmin审核 -> 通过（APPROVED）或拒绝（REJECTED）
 * 4. 通过：移动到articles表并删除草稿；拒绝：保留草稿并发送拒绝消息
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2025-07-31
 */
@Entity
@Table(name = "article_drafts")
public class ArticleDraft {
    
    /**
     * 审核状态枚举
     */
    public enum ReviewStatus {
        PENDING("待审核"),
        APPROVED("已通过"),
        REJECTED("已拒绝");
        
        private final String description;
        
        ReviewStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /** 草稿主键ID，自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "draft_id")
    private Long draftId;
    
    /** 关联的文章ID（如果是编辑现有文章，否则为null表示新建文章） */
    @Column(name = "article_id")
    private Long articleId;
    
    /** 编辑者用户ID */
    @NotNull(message = "编辑者用户ID不能为空")
    @Column(name = "editor_user_id", nullable = false)
    private Long editorUserId;
    
    /** 文章标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    @Column(nullable = false, length = 255)
    private String title;
    
    /** 文章内容（大文本字段） */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /** 文章分类 */
    @Size(max = 50, message = "分类长度不能超过50个字符")
    @Column(length = 50)
    private String category;
    
    /** 文章标签 */
    @Column(columnDefinition = "TEXT")
    private String tags;
    
    /** 审核状态 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;
    
    /** 提交时间 */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    /** 审核时间 */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    /** 提交原因/编辑说明（用户提交时的说明） */
    @Column(name = "submit_reason", columnDefinition = "TEXT")
    private String editNotes;
    
    /** 拒绝原因/审核备注 */
    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String reviewNotes;
    
    /** 审核者评论 */
    @Column(name = "reviewer_comment", columnDefinition = "TEXT")
    private String reviewerComment;
    
    /** 创建时间 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 构造函数
    public ArticleDraft() {}
    
    public ArticleDraft(Long articleId, Long editorUserId, String title, String content, 
                       String category, String tags, String editNotes) {
        this.articleId = articleId;
        this.editorUserId = editorUserId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.editNotes = editNotes;
        this.reviewStatus = ReviewStatus.PENDING;
        this.submittedAt = LocalDateTime.now();
    }
    
    // Getter 和 Setter 方法
    public Long getDraftId() {
        return draftId;
    }
    
    public void setDraftId(Long draftId) {
        this.draftId = draftId;
    }
    
    public Long getArticleId() {
        return articleId;
    }
    
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    
    public Long getEditorUserId() {
        return editorUserId;
    }
    
    public void setEditorUserId(Long editorUserId) {
        this.editorUserId = editorUserId;
    }
    
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
    
    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }
    
    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
    
    public String getReviewNotes() {
        return reviewNotes;
    }
    
    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
    
    public String getEditNotes() {
        return editNotes;
    }
    
    public void setEditNotes(String editNotes) {
        this.editNotes = editNotes;
    }
    
    public String getReviewerComment() {
        return reviewerComment;
    }
    
    public void setReviewerComment(String reviewerComment) {
        this.reviewerComment = reviewerComment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 判断是否为新建文章的草稿
     */
    public boolean isNewArticle() {
        return this.articleId == null;
    }
    
    /**
     * 判断是否为编辑现有文章的草稿
     */
    public boolean isEditingExisting() {
        return this.articleId != null;
    }
    
    /**
     * 标记为已审核通过
     */
    public void approve(String reviewNotes) {
        this.reviewStatus = ReviewStatus.APPROVED;
        this.reviewNotes = reviewNotes;
        this.reviewedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 标记为已拒绝
     */
    public void reject(String reviewNotes) {
        this.reviewStatus = ReviewStatus.REJECTED;
        this.reviewNotes = reviewNotes;
        this.reviewedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
