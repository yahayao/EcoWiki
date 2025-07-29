package com.ecowiki.entity.review;

import java.time.LocalDateTime;

import com.ecowiki.entity.article.ArticleReview;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * 审核权限配置实体类
 * 
 * 管理基于角色的审核权限配置
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-07-25
 */
@Entity
@Table(name = "review_permission_config")
public class ReviewPermissionConfig {
    
    /**
     * 配置ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;
    
    /**
     * 角色名称
     */
    @NotNull(message = "角色名称不能为空")
    @Column(name = "role_name", nullable = false)
    private String roleName;
    
    /**
     * 审核类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ArticleReview.ReviewType reviewType;
    
    /**
     * 是否能够分配审核员
     */
    @Column(name = "can_assign_reviewer")
    private Boolean canAssignReviewer = false;
    
    /**
     * 是否能够自审
     */
    @Column(name = "can_self_review")
    private Boolean canSelfReview = false;
    
    /**
     * 是否自动分配
     */
    @Column(name = "auto_assignment")
    private Boolean autoAssignment = true;
    
    /**
     * 权重（用于自动分配）
     */
    @Column(name = "weight")
    private Integer weight = 1;
    
    /**
     * 最大同时审核数量
     */
    @Column(name = "max_concurrent_reviews")
    private Integer maxConcurrentReviews = 10;
    
    /**
     * 优先级级别（可审核的优先级）
     */
    @Column(name = "priority_level")
    private Integer priorityLevel = 1;
    
    /**
     * 是否激活
     */
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public ReviewPermissionConfig() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ReviewPermissionConfig(String roleName, ArticleReview.ReviewType reviewType) {
        this();
        this.roleName = roleName;
        this.reviewType = reviewType;
    }
    
    // Getters and Setters
    public Long getConfigId() {
        return configId;
    }
    
    public void setConfigId(Long configId) {
        this.configId = configId;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    public ArticleReview.ReviewType getReviewType() {
        return reviewType;
    }
    
    public void setReviewType(ArticleReview.ReviewType reviewType) {
        this.reviewType = reviewType;
    }
    
    public Boolean getCanAssignReviewer() {
        return canAssignReviewer;
    }
    
    public void setCanAssignReviewer(Boolean canAssignReviewer) {
        this.canAssignReviewer = canAssignReviewer;
    }
    
    public Boolean getCanSelfReview() {
        return canSelfReview;
    }
    
    public void setCanSelfReview(Boolean canSelfReview) {
        this.canSelfReview = canSelfReview;
    }
    
    public Boolean getAutoAssignment() {
        return autoAssignment;
    }
    
    public void setAutoAssignment(Boolean autoAssignment) {
        this.autoAssignment = autoAssignment;
    }
    
    public Integer getWeight() {
        return weight;
    }
    
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
    
    public Integer getMaxConcurrentReviews() {
        return maxConcurrentReviews;
    }
    
    public void setMaxConcurrentReviews(Integer maxConcurrentReviews) {
        this.maxConcurrentReviews = maxConcurrentReviews;
    }
    
    public Integer getPriorityLevel() {
        return priorityLevel;
    }
    
    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "ReviewPermissionConfig{" +
                "configId=" + configId +
                ", roleName='" + roleName + '\'' +
                ", reviewType=" + reviewType +
                ", canAssignReviewer=" + canAssignReviewer +
                ", canSelfReview=" + canSelfReview +
                ", isActive=" + isActive +
                '}';
    }
}
