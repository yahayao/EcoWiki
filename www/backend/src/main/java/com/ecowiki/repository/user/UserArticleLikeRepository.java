/**
 * 用户文章点赞数据访问层接口
 * 
 * 功能：
 * - 提供用户点赞文章的数据访问接口
 * - 支持点赞记录的增删查等操作
 * - 支持分页查询和统计功能
 * - 提供用户点赞管理相关方法
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.repository.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.user.UserArticleLike;
@Repository
public interface UserArticleLikeRepository extends JpaRepository<UserArticleLike, Long> {
    
    /**
     * 根据用户ID和文章ID查找点赞记录
     */
    Optional<UserArticleLike> findByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * 检查用户是否点赞了某篇文章
     */
    boolean existsByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * 获取用户点赞的所有文章ID
     */
    @Query("SELECT l.articleId FROM UserArticleLike l WHERE l.userId = :userId ORDER BY l.likeTime DESC")
    List<Long> findArticleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 分页获取用户点赞的文章ID
     */
    @Query("SELECT l.articleId FROM UserArticleLike l WHERE l.userId = :userId ORDER BY l.likeTime DESC")
    Page<Long> findArticleIdsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 获取用户点赞的文章数量
     */
    long countByUserId(Long userId);
    
    /**
     * 根据用户ID获取点赞记录
     */
    List<UserArticleLike> findByUserIdOrderByLikeTimeDesc(Long userId);
    
    /**
     * 分页获取用户点赞记录
     */
    Page<UserArticleLike> findByUserIdOrderByLikeTimeDesc(Long userId, Pageable pageable);
    
    /**
     * 删除用户对某篇文章的点赞
     */
    void deleteByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * 获取某篇文章的点赞数量
     */
    long countByArticleId(Long articleId);
}
