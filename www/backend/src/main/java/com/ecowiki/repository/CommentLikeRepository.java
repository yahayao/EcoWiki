package com.ecowiki.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.entity.comment.CommentLike;

/**
 * 评论点赞Repository接口
 * 
 * 提供评论点赞数据访问功能，匹配实际数据库结构
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2025-01-21
 */
@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    
    /**
     * 查找用户对特定评论的点赞记录
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 点赞记录（如果存在）
     */
    @Query("SELECT cl FROM CommentLike cl WHERE cl.commentId = :commentId AND cl.userId = :userId")
    Optional<CommentLike> findByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);
    
    /**
     * 统计评论的点赞数量
     * 
     * @param commentId 评论ID
     * @return 点赞数量
     */
    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = :commentId")
    long countByCommentId(@Param("commentId") Long commentId);
    
    /**
     * 检查用户是否点赞了某个评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否点赞
     */
    @Query("SELECT CASE WHEN COUNT(cl) > 0 THEN true ELSE false END FROM CommentLike cl WHERE cl.commentId = :commentId AND cl.userId = :userId")
    boolean isCommentLikedByUser(@Param("commentId") Long commentId, @Param("userId") Long userId);
    
    /**
     * 删除用户对评论的点赞记录
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM CommentLike cl WHERE cl.commentId = :commentId AND cl.userId = :userId")
    void deleteByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);
}

