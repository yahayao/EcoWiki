/**
 * 用户文章收藏数据访问层接口
 * 
 * 功能：
 * - 提供用户收藏文章的数据访问接口
 * - 支持收藏记录的增删查等操作
 * - 支持分页查询和统计功能
 * - 提供用户收藏管理相关方法
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

import com.ecowiki.entity.user.UserArticleFavorite;
@Repository
public interface UserArticleFavoriteRepository extends JpaRepository<UserArticleFavorite, Long> {
    
    /**
     * 根据用户ID和文章ID查找收藏记录
     */
    Optional<UserArticleFavorite> findByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * 检查用户是否收藏了某篇文章
     */
    boolean existsByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * 获取用户收藏的所有文章ID
     */
    @Query("SELECT f.articleId FROM UserArticleFavorite f WHERE f.userId = :userId ORDER BY f.favoriteTime DESC")
    List<Long> findArticleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 分页获取用户收藏的文章ID
     */
    @Query("SELECT f.articleId FROM UserArticleFavorite f WHERE f.userId = :userId ORDER BY f.favoriteTime DESC")
    Page<Long> findArticleIdsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 获取用户收藏的文章数量
     */
    long countByUserId(Long userId);
    
    /**
     * 根据用户ID获取收藏记录
     */
    List<UserArticleFavorite> findByUserIdOrderByFavoriteTimeDesc(Long userId);
    
    /**
     * 分页获取用户收藏记录
     */
    Page<UserArticleFavorite> findByUserIdOrderByFavoriteTimeDesc(Long userId, Pageable pageable);
    
    /**
     * 删除用户对某篇文章的收藏
     */
    void deleteByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * 获取某篇文章的收藏数量
     */
    long countByArticleId(Long articleId);
}
