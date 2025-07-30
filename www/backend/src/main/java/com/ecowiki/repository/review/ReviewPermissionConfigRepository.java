package com.ecowiki.repository.review;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.article.ArticleReview;
import com.ecowiki.entity.review.ReviewPermissionConfig;

/**
 * 审核权限配置Repository接口
 * 
 * 提供审核权限配置相关的数据访问操作
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-07-25
 */
@Repository
public interface ReviewPermissionConfigRepository extends JpaRepository<ReviewPermissionConfig, Long> {
    
    /**
     * 根据角色名称查找配置
     */
    List<ReviewPermissionConfig> findByRoleName(String roleName);
    
    /**
     * 根据角色名称和审核类型查找配置
     */
    Optional<ReviewPermissionConfig> findByRoleNameAndReviewType(String roleName, ArticleReview.ReviewType reviewType);
    
    /**
     * 根据审核类型查找所有激活的配置
     */
    List<ReviewPermissionConfig> findByReviewTypeAndIsActiveTrue(ArticleReview.ReviewType reviewType);
    
    /**
     * 查找所有激活的配置
     */
    List<ReviewPermissionConfig> findByIsActiveTrue();
    
    /**
     * 查找支持自动分配的配置
     */
    List<ReviewPermissionConfig> findByAutoAssignmentTrueAndIsActiveTrue();
    
    /**
     * 根据审核类型查找支持自动分配的配置
     */
    List<ReviewPermissionConfig> findByReviewTypeAndAutoAssignmentTrueAndIsActiveTrue(ArticleReview.ReviewType reviewType);
    
    /**
     * 查找可以分配审核员的角色配置
     */
    List<ReviewPermissionConfig> findByCanAssignReviewerTrueAndIsActiveTrue();
    
    /**
     * 查找可以自审的角色配置
     */
    List<ReviewPermissionConfig> findByCanSelfReviewTrueAndIsActiveTrue();
    
    /**
     * 根据优先级级别查找配置
     */
    @Query("SELECT rpc FROM ReviewPermissionConfig rpc WHERE rpc.priorityLevel >= :minPriority AND rpc.isActive = true")
    List<ReviewPermissionConfig> findByMinPriorityLevel(@Param("minPriority") Integer minPriority);
    
    /**
     * 根据审核类型和优先级查找配置
     */
    @Query("SELECT rpc FROM ReviewPermissionConfig rpc WHERE rpc.reviewType = :reviewType AND rpc.priorityLevel >= :minPriority AND rpc.isActive = true")
    List<ReviewPermissionConfig> findByReviewTypeAndMinPriorityLevel(@Param("reviewType") ArticleReview.ReviewType reviewType, 
                                                                      @Param("minPriority") Integer minPriority);
    
    /**
     * 查找特定角色列表的配置
     */
    @Query("SELECT rpc FROM ReviewPermissionConfig rpc WHERE rpc.roleName IN :roleNames AND rpc.isActive = true")
    List<ReviewPermissionConfig> findByRoleNamesAndActive(@Param("roleNames") List<String> roleNames);
    
    /**
     * 根据权重排序查找自动分配配置
     */
    @Query("SELECT rpc FROM ReviewPermissionConfig rpc WHERE rpc.reviewType = :reviewType AND rpc.autoAssignment = true AND rpc.isActive = true ORDER BY rpc.weight DESC")
    List<ReviewPermissionConfig> findAutoAssignmentConfigsByWeight(@Param("reviewType") ArticleReview.ReviewType reviewType);
    
    /**
     * 检查角色是否有指定审核类型的权限
     */
    @Query("SELECT CASE WHEN COUNT(rpc) > 0 THEN true ELSE false END FROM ReviewPermissionConfig rpc WHERE rpc.roleName = :roleName AND rpc.reviewType = :reviewType AND rpc.isActive = true")
    boolean hasReviewPermission(@Param("roleName") String roleName, @Param("reviewType") ArticleReview.ReviewType reviewType);
    
    /**
     * 检查角色是否可以分配审核员
     */
    @Query("SELECT CASE WHEN COUNT(rpc) > 0 THEN true ELSE false END FROM ReviewPermissionConfig rpc WHERE rpc.roleName = :roleName AND rpc.canAssignReviewer = true AND rpc.isActive = true")
    boolean canAssignReviewer(@Param("roleName") String roleName);
    
    /**
     * 检查角色是否可以自审
     */
    @Query("SELECT CASE WHEN COUNT(rpc) > 0 THEN true ELSE false END FROM ReviewPermissionConfig rpc WHERE rpc.roleName = :roleName AND rpc.canSelfReview = true AND rpc.isActive = true")
    boolean canSelfReview(@Param("roleName") String roleName);
    
    /**
     * 获取角色的最大同时审核数量
     */
    @Query("SELECT COALESCE(MAX(rpc.maxConcurrentReviews), 0) FROM ReviewPermissionConfig rpc WHERE rpc.roleName = :roleName AND rpc.isActive = true")
    Integer getMaxConcurrentReviews(@Param("roleName") String roleName);
    
    /**
     * 查找所有不同的角色名称
     */
    @Query("SELECT DISTINCT rpc.roleName FROM ReviewPermissionConfig rpc WHERE rpc.isActive = true")
    List<String> findAllActiveRoleNames();
    
    /**
     * 根据角色名称列表和审核类型查找配置
     */
    @Query("SELECT rpc FROM ReviewPermissionConfig rpc WHERE rpc.roleName IN :roleNames AND rpc.reviewType = :reviewType AND rpc.isActive = true")
    List<ReviewPermissionConfig> findByRoleNamesAndReviewType(@Param("roleNames") List<String> roleNames, 
                                                              @Param("reviewType") ArticleReview.ReviewType reviewType);
}
