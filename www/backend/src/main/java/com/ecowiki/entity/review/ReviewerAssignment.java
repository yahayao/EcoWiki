package com.ecowiki.entity.review;

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
 * 审核员分配实体类
 * 
 * 管理审核任务的审核员分配记录
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-07-25
 */
@Entity
@Table(name = "reviewer_assignment")
public class ReviewerAssignment {
    
    /**
     * 分配ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;
    
    /**
     * 审核ID
     */
    @NotNull(message = "审核ID不能为空")
    @Column(name = "review_id", nullable = false)
    private Long reviewId;
    
    /**
     * 审核员ID
     */
    @NotNull(message = "审核员ID不能为空")
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;
    
    /**
     * 分配员ID（谁进行的分配）
     */
    @Column(name = "assigner_id")
    private Long assignerId;
    
    /**
     * 分配状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentStatus status = AssignmentStatus.ACTIVE;
    
    /**
     * 分配时间
     */
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    /**
     * 接受时间
     */
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    /**
     * 拒绝时间
     */
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    
    /**
     * 分配原因/备注
     */
    @Column(name = "assignment_reason", columnDefinition = "TEXT")
    private String assignmentReason;
    
    /**
     * 是否自动分配
     */
    @Column(name = "auto_assigned")
    private Boolean autoAssigned = false;
    
    /**
     * 权重分数（用于自动分配算法）
     */
    @Column(name = "weight_score")
    private Double weightScore;
    
    /**
     * 预期完成时间
     */
    @Column(name = "expected_completion_time")
    private LocalDateTime expectedCompletionTime;
    
    /**
     * 分配状态枚举
     */
    public enum AssignmentStatus {
        ACTIVE("活跃"),
        ACCEPTED("已接受"),
        REJECTED("已拒绝"),
        CANCELLED("已取消"),
        COMPLETED("已完成");
        
        private final String description;
        
        AssignmentStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 构造函数
    public ReviewerAssignment() {
        this.assignedAt = LocalDateTime.now();
    }
    
    public ReviewerAssignment(Long reviewId, Long reviewerId) {
        this();
        this.reviewId = reviewId;
        this.reviewerId = reviewerId;
    }
    
    public ReviewerAssignment(Long reviewId, Long reviewerId, Long assignerId, boolean autoAssigned) {
        this(reviewId, reviewerId);
        this.assignerId = assignerId;
        this.autoAssigned = autoAssigned;
    }
    
    // Getters and Setters
    public Long getAssignmentId() {
        return assignmentId;
    }
    
    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }
    
    public Long getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }
    
    public Long getReviewerId() {
        return reviewerId;
    }
    
    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }
    
    public Long getAssignerId() {
        return assignerId;
    }
    
    public void setAssignerId(Long assignerId) {
        this.assignerId = assignerId;
    }
    
    public AssignmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }
    
    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
    
    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }
    
    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }
    
    public String getAssignmentReason() {
        return assignmentReason;
    }
    
    public void setAssignmentReason(String assignmentReason) {
        this.assignmentReason = assignmentReason;
    }
    
    public Boolean getAutoAssigned() {
        return autoAssigned;
    }
    
    public void setAutoAssigned(Boolean autoAssigned) {
        this.autoAssigned = autoAssigned;
    }
    
    public Double getWeightScore() {
        return weightScore;
    }
    
    public void setWeightScore(Double weightScore) {
        this.weightScore = weightScore;
    }
    
    public LocalDateTime getExpectedCompletionTime() {
        return expectedCompletionTime;
    }
    
    public void setExpectedCompletionTime(LocalDateTime expectedCompletionTime) {
        this.expectedCompletionTime = expectedCompletionTime;
    }
    
    /**
     * 接受分配
     */
    public void accept() {
        this.status = AssignmentStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }
    
    /**
     * 拒绝分配
     */
    public void reject() {
        this.status = AssignmentStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
    }
    
    /**
     * 取消分配
     */
    public void cancel() {
        this.status = AssignmentStatus.CANCELLED;
    }
    
    /**
     * 完成分配
     */
    public void complete() {
        this.status = AssignmentStatus.COMPLETED;
    }
    
    /**
     * 检查是否过期
     */
    public boolean isOverdue() {
        return expectedCompletionTime != null && 
               LocalDateTime.now().isAfter(expectedCompletionTime) && 
               status == AssignmentStatus.ACTIVE;
    }
    
    @Override
    public String toString() {
        return "ReviewerAssignment{" +
                "assignmentId=" + assignmentId +
                ", reviewId=" + reviewId +
                ", reviewerId=" + reviewerId +
                ", status=" + status +
                ", assignedAt=" + assignedAt +
                ", autoAssigned=" + autoAssigned +
                '}';
    }
}
