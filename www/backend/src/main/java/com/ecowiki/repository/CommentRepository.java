package com.ecowiki.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.Comment;

/**
 * 评论Repository接口
 * 
 * 提供评论数据访问功能，匹配实际数据库表结构
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2025-01-21
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * 根据文章ID分页查询顶级评论（非回复）
     * 
     * @param articleId 文章ID
     * @param pageable 分页参数
     * @return 顶级评论列表
     */
    @Query("SELECT c FROM Comment c WHERE c.articleId = :articleId AND c.parentCommentId IS NULL AND c.deleted = false ORDER BY c.commentTime DESC")
    Page<Comment> findTopLevelCommentsByArticleId(@Param("articleId") Long articleId, Pageable pageable);
    
    /**
     * 根据文章ID查询所有评论（包括回复）
     * 
     * @param articleId 文章ID
     * @param pageable 分页参数
     * @return 所有评论列表
     */
    @Query("SELECT c FROM Comment c WHERE c.articleId = :articleId AND c.deleted = false")
    Page<Comment> findAllCommentsByArticleId(@Param("articleId") Long articleId, Pageable pageable);
    
    /**
     * 根据父评论ID查询回复列表
     * 
     * @param parentCommentId 父评论ID
     * @return 回复列表
     */
    @Query("SELECT c FROM Comment c WHERE c.parentCommentId = :parentCommentId AND c.deleted = false ORDER BY c.commentTime ASC")
    List<Comment> findRepliesByParentId(@Param("parentCommentId") Long parentCommentId);
    
    /**
     * 根据用户ID查询评论
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 评论列表
     */
    @Query("SELECT c FROM Comment c WHERE c.userId = :userId AND c.deleted = false ORDER BY c.commentTime DESC")
    Page<Comment> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 统计文章的评论数量（不包括回复）
     * 
     * @param articleId 文章ID
     * @return 评论数量
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.articleId = :articleId AND c.parentCommentId IS NULL AND c.deleted = false")
    long countTopLevelCommentsByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 统计文章的总评论数量（包括回复）
     * 
     * @param articleId 文章ID
     * @return 总评论数量
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.articleId = :articleId AND c.deleted = false")
    long countAllCommentsByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 统计用户的评论数量
     * 
     * @param userId 用户ID
     * @return 评论数量
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.userId = :userId AND c.deleted = false")
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * 根据文章ID和排序方式查询顶级评论
     * 
     * @param articleId 文章ID
     * @param pageable 分页参数（包含排序信息）
     * @return 评论列表
     */
    @Query("SELECT c FROM Comment c WHERE c.articleId = :articleId AND c.parentCommentId IS NULL AND c.deleted = false")
    Page<Comment> findTopLevelCommentsByArticleIdWithSort(@Param("articleId") Long articleId, Pageable pageable);
    
    /**
     * 查询最受欢迎的评论（按点赞数排序）
     * 
     * @param articleId 文章ID
     * @param pageable 分页参数
     * @return 热门评论列表
     */
    @Query("SELECT c FROM Comment c WHERE c.articleId = :articleId AND c.parentCommentId IS NULL AND c.deleted = false ORDER BY c.likes DESC, c.commentTime DESC")
    Page<Comment> findHotCommentsByArticleId(@Param("articleId") Long articleId, Pageable pageable);
    
    /**
     * 查询最新评论
     * 
     * @param articleId 文章ID
     * @param pageable 分页参数
     * @return 最新评论列表
     */
    @Query("SELECT c FROM Comment c WHERE c.articleId = :articleId AND c.parentCommentId IS NULL AND c.deleted = false ORDER BY c.commentTime DESC")
    Page<Comment> findNewestCommentsByArticleId(@Param("articleId") Long articleId, Pageable pageable);
    
    /**
     * 查询最早评论
     * 
     * @param articleId 文章ID
     * @param pageable 分页参数
     * @return 最早评论列表
     */
    @Query("SELECT c FROM Comment c WHERE c.articleId = :articleId AND c.parentCommentId IS NULL AND c.deleted = false ORDER BY c.commentTime ASC")
    Page<Comment> findOldestCommentsByArticleId(@Param("articleId") Long articleId, Pageable pageable);
    
    /**
     * 根据内容搜索评论
     * 
     * @param articleId 文章ID
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 匹配的评论列表
     */
    @Query("SELECT c FROM Comment c WHERE c.articleId = :articleId AND c.content LIKE %:keyword% AND c.deleted = false ORDER BY c.commentTime DESC")
    Page<Comment> searchCommentsByContent(@Param("articleId") Long articleId, @Param("keyword") String keyword, Pageable pageable);
}

