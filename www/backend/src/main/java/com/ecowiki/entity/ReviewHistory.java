package com.ecowiki.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * 审核历史实体类
 * 
 * 记录审核过程中的所有操作历史，用于审计和追踪
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-07-25
 */
@Entity
@Table(name = "review_history")
public class ReviewHistory {
    
    /**
     * 历史记录ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;
    
    /**
     * 审核ID
     */
    @NotNull(message = "审核ID不能为空")
    @Column(name = "review_id", nullable = false)
    private Long reviewId;
    
    /**
     * 操作员ID
     */
    @NotNull(message = "操作员ID不能为空")
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
    
    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;
    
    /**
     * 原状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private ArticleReview.ReviewStatus oldStatus;
    
    /**
     * 新状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private ArticleReview.ReviewStatus newStatus;
    
    /**
     * 操作描述/备注
     */
    @Column(name = "action_description", columnDefinition = "TEXT")
    private String actionDescription;
    
    /**
     * 操作时间
     */
    @Column(name = "action_time")
    private LocalDateTime actionTime;
    
    /**
     * IP地址
     */
    @Column(name = "ip_address")
    private String ipAddress;
    
    /**
     * 用户代理（浏览器信息）
     */
    @Column(name = "user_agent")
    private String userAgent;
    
    /**
     * 额外数据（JSON格式）
     */
    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;
    
    /**
     * 操作类型枚举
     */
    public enum ActionType {
        CREATED("创建审核"),
        ASSIGNED("分配审核员"),
        ACCEPTED("接受审核"),
        REJECTED_ASSIGNMENT("拒绝分配"),
        APPROVED("审核通过"),
        REJECTED("审核拒绝"),
        CANCELLED("取消审核"),
        MODIFIED("修改审核"),
        REASSIGNED("重新分配"),
        DEADLINE_EXTENDED("延长截止时间"),
        PRIORITY_CHANGED("更改优先级"),
        COMMENT_ADDED("添加评论");
        
        private final String description;
        
        ActionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 构造函数
    public ReviewHistory() {
        this.actionTime = LocalDateTime.now();
    }
    
    public ReviewHistory(Long reviewId, Long operatorId, ActionType actionType) {
        this();
        this.reviewId = reviewId;
        this.operatorId = operatorId;
        this.actionType = actionType;
    }
    
    public ReviewHistory(Long reviewId, Long operatorId, ActionType actionType, 
                        ArticleReview.ReviewStatus oldStatus, ArticleReview.ReviewStatus newStatus) {
        this(reviewId, operatorId, actionType);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
    
    // Getters and Setters
    public Long getHistoryId() {
        return historyId;
    }
    
    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }
    
    public Long getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }
    
    public Long getOperatorId() {
        return operatorId;
    }
    
    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
    
    public ArticleReview.ReviewStatus getOldStatus() {
        return oldStatus;
    }
    
    public void setOldStatus(ArticleReview.ReviewStatus oldStatus) {
        this.oldStatus = oldStatus;
    }
    
    public ArticleReview.ReviewStatus getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(ArticleReview.ReviewStatus newStatus) {
        this.newStatus = newStatus;
    }
    
    public String getActionDescription() {
        return actionDescription;
    }
    
    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }
    
    public LocalDateTime getActionTime() {
        return actionTime;
    }
    
    public void setActionTime(LocalDateTime actionTime) {
        this.actionTime = actionTime;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getExtraData() {
        return extraData;
    }
    
    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
    
    @Override
    public String toString() {
        return "ReviewHistory{" +
                "historyId=" + historyId +
                ", reviewId=" + reviewId +
                ", operatorId=" + operatorId +
                ", actionType=" + actionType +
                ", actionTime=" + actionTime +
                '}';
    }
}
