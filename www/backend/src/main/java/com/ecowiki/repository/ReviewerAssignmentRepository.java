package com.ecowiki.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.ReviewerAssignment;

/**
 * 审核员分配Repository接口
 * 
 * 提供审核员分配相关的数据访问操作
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-07-25
 */
@Repository
public interface ReviewerAssignmentRepository extends JpaRepository<ReviewerAssignment, Long> {
    
    /**
     * 根据审核ID查找所有分配记录
     */
    List<ReviewerAssignment> findByReviewId(Long reviewId);
    
    /**
     * 根据审核ID查找当前活跃的分配记录
     */
    List<ReviewerAssignment> findByReviewIdAndStatus(Long reviewId, ReviewerAssignment.AssignmentStatus status);
    
    /**
     * 根据审核员ID查找分配记录
     */
    List<ReviewerAssignment> findByReviewerId(Long reviewerId);
    
    /**
     * 根据审核员ID和状态查找分配记录
     */
    List<ReviewerAssignment> findByReviewerIdAndStatus(Long reviewerId, ReviewerAssignment.AssignmentStatus status);
    
    /**
     * 根据分配员ID查找分配记录
     */
    List<ReviewerAssignment> findByAssignerId(Long assignerId);
    
    /**
     * 查找审核员当前活跃的分配数量
     */
    @Query("SELECT COUNT(ra) FROM ReviewerAssignment ra WHERE ra.reviewerId = :reviewerId AND ra.status IN ('ACTIVE', 'ACCEPTED')")
    Long countActiveAssignmentsByReviewer(@Param("reviewerId") Long reviewerId);
    
    /**
     * 查找过期的分配记录
     */
    @Query("SELECT ra FROM ReviewerAssignment ra WHERE ra.status = 'ACTIVE' AND ra.expectedCompletionTime < :currentTime")
    List<ReviewerAssignment> findOverdueAssignments(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 查找自动分配的记录
     */
    List<ReviewerAssignment> findByAutoAssignedTrue();
    
    /**
     * 查找手动分配的记录
     */
    List<ReviewerAssignment> findByAutoAssignedFalse();
    
    /**
     * 根据权重分数排序查找分配记录
     */
    @Query("SELECT ra FROM ReviewerAssignment ra WHERE ra.reviewId = :reviewId ORDER BY ra.weightScore DESC")
    List<ReviewerAssignment> findByReviewIdOrderByWeightScoreDesc(@Param("reviewId") Long reviewId);
    
    /**
     * 查找审核员在指定时间范围内的分配统计
     */
    @Query(value = "SELECT " +
           "COUNT(*) as total_assignments, " +
           "SUM(CASE WHEN status = 'ACCEPTED' THEN 1 ELSE 0 END) as accepted_count, " +
           "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected_count, " +
           "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_count, " +
           "SUM(CASE WHEN auto_assigned = true THEN 1 ELSE 0 END) as auto_assigned_count " +
           "FROM reviewer_assignment " +
           "WHERE reviewer_id = :reviewerId " +
           "AND assigned_at BETWEEN :startTime AND :endTime", nativeQuery = true)
    Object[] getAssignmentStatistics(@Param("reviewerId") Long reviewerId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找审核的当前分配记录
     */
    @Query("SELECT ra FROM ReviewerAssignment ra WHERE ra.reviewId = :reviewId AND ra.status IN ('ACTIVE', 'ACCEPTED') ORDER BY ra.assignedAt DESC")
    List<ReviewerAssignment> findCurrentAssignmentsByReview(@Param("reviewId") Long reviewId);
    
    /**
     * 查找最新的分配记录
     */
    Optional<ReviewerAssignment> findTopByReviewIdOrderByAssignedAtDesc(Long reviewId);
    
    /**
     * 检查审核是否已分配给特定审核员
     */
    @Query("SELECT CASE WHEN COUNT(ra) > 0 THEN true ELSE false END FROM ReviewerAssignment ra WHERE ra.reviewId = :reviewId AND ra.reviewerId = :reviewerId AND ra.status IN ('ACTIVE', 'ACCEPTED')")
    boolean isAssignedToReviewer(@Param("reviewId") Long reviewId, @Param("reviewerId") Long reviewerId);
    
    /**
     * 查找待接受的分配记录
     */
    @Query("SELECT ra FROM ReviewerAssignment ra WHERE ra.reviewerId = :reviewerId AND ra.status = 'ACTIVE' ORDER BY ra.assignedAt ASC")
    List<ReviewerAssignment> findPendingAssignmentsByReviewer(@Param("reviewerId") Long reviewerId);
    
    /**
     * 根据多个状态查找分配记录
     */
    @Query("SELECT ra FROM ReviewerAssignment ra WHERE ra.status IN :statuses ORDER BY ra.assignedAt DESC")
    Page<ReviewerAssignment> findByStatusIn(@Param("statuses") List<ReviewerAssignment.AssignmentStatus> statuses, Pageable pageable);
    
    /**
     * 查找即将过期的分配记录（2小时内）
     */
    @Query("SELECT ra FROM ReviewerAssignment ra WHERE ra.status = 'ACTIVE' AND ra.expectedCompletionTime BETWEEN :currentTime AND :deadlineTime")
    List<ReviewerAssignment> findUpcomingDeadlines(@Param("currentTime") LocalDateTime currentTime,
                                                   @Param("deadlineTime") LocalDateTime deadlineTime);
    
    /**
     * 统计分配员的分配记录数量
     */
    @Query("SELECT COUNT(ra) FROM ReviewerAssignment ra WHERE ra.assignerId = :assignerId AND ra.assignedAt BETWEEN :startTime AND :endTime")
    Long countAssignmentsByAssigner(@Param("assignerId") Long assignerId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找审核员的历史分配记录（分页）
     */
    Page<ReviewerAssignment> findByReviewerIdOrderByAssignedAtDesc(Long reviewerId, Pageable pageable);
    
    /**
     * 查找审核的所有历史分配记录
     */
    @Query("SELECT ra FROM ReviewerAssignment ra WHERE ra.reviewId = :reviewId ORDER BY ra.assignedAt DESC")
    List<ReviewerAssignment> findAllAssignmentHistoryByReview(@Param("reviewId") Long reviewId);
    
    /**
     * 根据权重分数范围查找分配记录
     */
    @Query("SELECT ra FROM ReviewerAssignment ra WHERE ra.weightScore BETWEEN :minWeight AND :maxWeight ORDER BY ra.weightScore DESC")
    List<ReviewerAssignment> findByWeightScoreRange(@Param("minWeight") Double minWeight, @Param("maxWeight") Double maxWeight);
}
