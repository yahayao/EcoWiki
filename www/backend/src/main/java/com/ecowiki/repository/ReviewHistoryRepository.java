package com.ecowiki.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.review.ReviewHistory;

/**
 * 审核历史Repository接口
 * 
 * 提供审核历史相关的数据访问操作
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-07-25
 */
@Repository
public interface ReviewHistoryRepository extends JpaRepository<ReviewHistory, Long> {
    
    /**
     * 根据审核ID查找历史记录
     */
    List<ReviewHistory> findByReviewIdOrderByActionTimeDesc(Long reviewId);
    
    /**
     * 根据操作员ID查找历史记录
     */
    List<ReviewHistory> findByOperatorIdOrderByActionTimeDesc(Long operatorId);
    
    /**
     * 根据操作类型查找历史记录
     */
    List<ReviewHistory> findByActionType(ReviewHistory.ActionType actionType);
    
    /**
     * 根据审核ID和操作类型查找历史记录
     */
    List<ReviewHistory> findByReviewIdAndActionType(Long reviewId, ReviewHistory.ActionType actionType);
    
    /**
     * 根据操作员ID和操作类型查找历史记录
     */
    List<ReviewHistory> findByOperatorIdAndActionType(Long operatorId, ReviewHistory.ActionType actionType);
    
    /**
     * 查找指定时间范围内的历史记录
     */
    @Query("SELECT rh FROM ReviewHistory rh WHERE rh.actionTime BETWEEN :startTime AND :endTime ORDER BY rh.actionTime DESC")
    List<ReviewHistory> findByActionTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找审核的最新操作记录
     */
    @Query("SELECT rh FROM ReviewHistory rh WHERE rh.reviewId = :reviewId ORDER BY rh.actionTime DESC LIMIT 1")
    ReviewHistory findLatestByReviewId(@Param("reviewId") Long reviewId);
    
    /**
     * 统计操作员在指定时间范围内的操作次数
     */
    @Query("SELECT COUNT(rh) FROM ReviewHistory rh WHERE rh.operatorId = :operatorId AND rh.actionTime BETWEEN :startTime AND :endTime")
    Long countOperationsByOperator(@Param("operatorId") Long operatorId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定操作类型在时间范围内的次数
     */
    @Query("SELECT COUNT(rh) FROM ReviewHistory rh WHERE rh.actionType = :actionType AND rh.actionTime BETWEEN :startTime AND :endTime")
    Long countActionsByType(@Param("actionType") ReviewHistory.ActionType actionType,
                            @Param("startTime") LocalDateTime startTime,
                            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找审核的完整操作链
     */
    @Query("SELECT rh FROM ReviewHistory rh WHERE rh.reviewId = :reviewId ORDER BY rh.actionTime ASC")
    List<ReviewHistory> findCompleteActionChain(@Param("reviewId") Long reviewId);
    
    /**
     * 根据IP地址查找历史记录
     */
    List<ReviewHistory> findByIpAddressOrderByActionTimeDesc(String ipAddress);
    
    /**
     * 查找操作员的历史记录（分页）
     */
    Page<ReviewHistory> findByOperatorIdOrderByActionTimeDesc(Long operatorId, Pageable pageable);
    
    /**
     * 查找多个操作类型的历史记录
     */
    @Query("SELECT rh FROM ReviewHistory rh WHERE rh.actionType IN :actionTypes ORDER BY rh.actionTime DESC")
    List<ReviewHistory> findByActionTypeIn(@Param("actionTypes") List<ReviewHistory.ActionType> actionTypes);
    
    /**
     * 统计各种操作类型的数量
     */
    @Query(value = "SELECT action_type, COUNT(*) as count " +
           "FROM review_history " +
           "WHERE action_time BETWEEN :startTime AND :endTime " +
           "GROUP BY action_type " +
           "ORDER BY count DESC", nativeQuery = true)
    List<Object[]> getActionTypeStatistics(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找审核状态变更历史
     */
    @Query("SELECT rh FROM ReviewHistory rh WHERE rh.reviewId = :reviewId AND rh.oldStatus IS NOT NULL AND rh.newStatus IS NOT NULL ORDER BY rh.actionTime ASC")
    List<ReviewHistory> findStatusChangeHistory(@Param("reviewId") Long reviewId);
    
    /**
     * 查找最近的操作记录
     */
    @Query("SELECT rh FROM ReviewHistory rh ORDER BY rh.actionTime DESC")
    Page<ReviewHistory> findRecentActions(Pageable pageable);
    
    /**
     * 根据操作员查找指定时间范围内的活动
     */
    @Query("SELECT rh FROM ReviewHistory rh WHERE rh.operatorId = :operatorId AND rh.actionTime BETWEEN :startTime AND :endTime ORDER BY rh.actionTime DESC")
    List<ReviewHistory> findOperatorActivityInPeriod(@Param("operatorId") Long operatorId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找包含特定描述关键字的历史记录
     */
    @Query("SELECT rh FROM ReviewHistory rh WHERE rh.actionDescription LIKE %:keyword% ORDER BY rh.actionTime DESC")
    List<ReviewHistory> findByActionDescriptionContaining(@Param("keyword") String keyword);
    
    /**
     * 统计审核的操作次数
     */
    @Query("SELECT COUNT(rh) FROM ReviewHistory rh WHERE rh.reviewId = :reviewId")
    Long countActionsByReview(@Param("reviewId") Long reviewId);
    
    /**
     * 查找审核员的操作类型统计
     */
    @Query(value = "SELECT action_type, COUNT(*) as count " +
           "FROM review_history " +
           "WHERE operator_id = :operatorId " +
           "AND action_time BETWEEN :startTime AND :endTime " +
           "GROUP BY action_type " +
           "ORDER BY count DESC", nativeQuery = true)
    List<Object[]> getOperatorActionStatistics(@Param("operatorId") Long operatorId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);
}
