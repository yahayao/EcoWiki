package com.ecowiki.repository.article;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.article.ArticleReview;

/**
 * 文章审核Repository接口
 * 
 * 提供文章审核相关的数据访问操作
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-07-25
 */
@Repository
public interface ArticleReviewRepository extends JpaRepository<ArticleReview, Long> {
    
    /**
     * 根据文章ID查找最新的审核记录
     */
    Optional<ArticleReview> findTopByArticleIdOrderBySubmitTimeDesc(Long articleId);
    
    /**
     * 根据审核员ID查找待审核的记录
     */
    List<ArticleReview> findByReviewerIdAndStatus(Long reviewerId, ArticleReview.ReviewStatus status);
    
    /**
     * 根据提交人ID查找审核记录
     */
    List<ArticleReview> findBySubmitterIdOrderBySubmitTimeDesc(Long submitterId);
    
    /**
     * 根据状态查找审核记录
     */
    Page<ArticleReview> findByStatus(ArticleReview.ReviewStatus status, Pageable pageable);
    
    /**
     * 根据审核类型查找审核记录
     */
    List<ArticleReview> findByReviewType(ArticleReview.ReviewType reviewType);
    
    /**
     * 查找过期的审核记录
     */
    @Query("SELECT ar FROM ArticleReview ar WHERE ar.status = 'PENDING' AND ar.reviewDeadline < :currentTime")
    List<ArticleReview> findOverdueReviews(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 查找即将过期的审核记录（2小时内）
     */
    @Query("SELECT ar FROM ArticleReview ar WHERE ar.status = 'PENDING' AND ar.reviewDeadline BETWEEN :currentTime AND :deadlineTime")
    List<ArticleReview> findUpcomingDeadlines(@Param("currentTime") LocalDateTime currentTime, 
                                              @Param("deadlineTime") LocalDateTime deadlineTime);
    
    /**
     * 根据优先级查找待审核记录
     */
    @Query("SELECT ar FROM ArticleReview ar WHERE ar.status = 'PENDING' AND ar.priorityLevel >= :minPriority ORDER BY ar.priorityLevel DESC, ar.submitTime ASC")
    List<ArticleReview> findPendingByPriority(@Param("minPriority") Integer minPriority);
    
    /**
     * 统计审核员的待审核数量
     */
    @Query("SELECT COUNT(ar) FROM ArticleReview ar WHERE ar.reviewerId = :reviewerId AND ar.status = 'PENDING'")
    Long countPendingReviewsByReviewer(@Param("reviewerId") Long reviewerId);
    
    /**
     * 统计指定时间范围内的审核数量
     */
    @Query("SELECT COUNT(ar) FROM ArticleReview ar WHERE ar.reviewTime BETWEEN :startTime AND :endTime AND ar.status IN ('APPROVED', 'REJECTED')")
    Long countCompletedReviewsBetween(@Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找审核员的审核历史（分页）
     */
    Page<ArticleReview> findByReviewerIdOrderByReviewTimeDesc(Long reviewerId, Pageable pageable);
    
    /**
     * 查找未分配审核员的待审核记录
     */
    @Query("SELECT ar FROM ArticleReview ar WHERE ar.status = 'PENDING' AND ar.reviewerId IS NULL ORDER BY ar.priorityLevel DESC, ar.submitTime ASC")
    List<ArticleReview> findUnassignedPendingReviews();
    
    /**
     * 根据文章ID和审核类型查找审核记录
     */
    List<ArticleReview> findByArticleIdAndReviewType(Long articleId, ArticleReview.ReviewType reviewType);
    
    /**
     * 查找特定用户在指定时间范围内的审核统计
     */
    @Query(value = "SELECT " +
           "COUNT(*) as total_reviews, " +
           "SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) as approved_count, " +
           "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected_count, " +
           "AVG(TIMESTAMPDIFF(HOUR, submit_time, review_time)) as avg_review_time_hours " +
           "FROM article_review " +
           "WHERE reviewer_id = :reviewerId " +
           "AND review_time BETWEEN :startTime AND :endTime " +
           "AND status IN ('APPROVED', 'REJECTED')", nativeQuery = true)
    Object[] getReviewerStatistics(@Param("reviewerId") Long reviewerId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找需要自动发布的已通过审核记录
     */
    @Query("SELECT ar FROM ArticleReview ar WHERE ar.status = 'APPROVED' AND ar.autoPublish = true")
    List<ArticleReview> findAutoPublishApprovedReviews();
    
    /**
     * 根据多个状态查找审核记录
     */
    @Query("SELECT ar FROM ArticleReview ar WHERE ar.status IN :statuses ORDER BY ar.submitTime DESC")
    Page<ArticleReview> findByStatusIn(@Param("statuses") List<ArticleReview.ReviewStatus> statuses, Pageable pageable);
    
    /**
     * 检查文章是否有正在进行的审核
     */
    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END FROM ArticleReview ar WHERE ar.articleId = :articleId AND ar.status = 'PENDING'")
    boolean hasActivePendingReview(@Param("articleId") Long articleId);
    
    /**
     * 查找高优先级待审核记录
     */
    @Query("SELECT ar FROM ArticleReview ar WHERE ar.status = 'PENDING' AND ar.priorityLevel >= 4 ORDER BY ar.priorityLevel DESC, ar.submitTime ASC")
    List<ArticleReview> findHighPriorityPendingReviews();
}
