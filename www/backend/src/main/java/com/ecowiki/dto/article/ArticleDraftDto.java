package com.ecowiki.dto.article;

import java.time.LocalDateTime;

import com.ecowiki.entity.article.ArticleDraft;

/**
 * 文章草稿数据传输对象
 * 
 * 用于前后端之间传递文章草稿数据，包含草稿的完整信息和审核状态。
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2025-07-31
 */
public class ArticleDraftDto {
    
    /** 草稿ID */
    private Long draftId;
    
    /** 关联的文章ID（新建文章时为null） */
    private Long articleId;
    
    /** 编辑者用户ID */
    private Long editorUserId;
    
    /** 编辑者用户名 */
    private String editorUsername;
    
    /** 文章标题 */
    private String title;
    
    /** 文章内容 */
    private String content;
    
    /** 文章分类 */
    private String category;
    
    /** 文章标签 */
    private String tags;
    
    /** 审核状态 */
    private String reviewStatus;
    
    /** 审核状态描述 */
    private String reviewStatusDescription;
    
    /** 提交时间 */
    private LocalDateTime submittedAt;
    
    /** 审核时间 */
    private LocalDateTime reviewedAt;
    
    /** 审核者用户ID */
    private Long reviewerUserId;
    
    /** 审核者用户名 */
    private String reviewerUsername;
    
    /** 审核备注 */
    private String reviewNotes;
    
    /** 编辑说明 */
    private String editNotes;
    
    /** 是否为新建文章 */
    private boolean isNewArticle;
    
    /** 原文章标题（编辑现有文章时显示） */
    private String originalTitle;
    
    // 构造函数
    public ArticleDraftDto() {}
    
    public ArticleDraftDto(ArticleDraft draft) {
        this.draftId = draft.getDraftId();
        this.articleId = draft.getArticleId();
        this.editorUserId = draft.getEditorUserId();
        this.title = draft.getTitle();
        this.content = draft.getContent();
        this.category = draft.getCategory();
        this.tags = draft.getTags();
        this.reviewStatus = draft.getReviewStatus().name();
        this.reviewStatusDescription = draft.getReviewStatus().getDescription();
        this.submittedAt = draft.getSubmittedAt();
        this.reviewedAt = draft.getReviewedAt();
        // reviewerUserId 字段已从实体中移除，设为null
        this.reviewerUserId = null;
        this.reviewNotes = draft.getReviewNotes();
        this.editNotes = draft.getEditNotes();
        this.isNewArticle = draft.isNewArticle();
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
    
    public String getEditorUsername() {
        return editorUsername;
    }
    
    public void setEditorUsername(String editorUsername) {
        this.editorUsername = editorUsername;
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
    
    public String getReviewStatus() {
        return reviewStatus;
    }
    
    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }
    
    public String getReviewStatusDescription() {
        return reviewStatusDescription;
    }
    
    public void setReviewStatusDescription(String reviewStatusDescription) {
        this.reviewStatusDescription = reviewStatusDescription;
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
    
    public Long getReviewerUserId() {
        return reviewerUserId;
    }
    
    public void setReviewerUserId(Long reviewerUserId) {
        this.reviewerUserId = reviewerUserId;
    }
    
    public String getReviewerUsername() {
        return reviewerUsername;
    }
    
    public void setReviewerUsername(String reviewerUsername) {
        this.reviewerUsername = reviewerUsername;
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
    
    public boolean isNewArticle() {
        return isNewArticle;
    }
    
    public void setNewArticle(boolean newArticle) {
        isNewArticle = newArticle;
    }
    
    public String getOriginalTitle() {
        return originalTitle;
    }
    
    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }
}
