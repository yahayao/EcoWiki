/**
 * 文章草稿数据访问层接口
 * 
 * 功能：
 * - 提供文章草稿的数据库操作接口
 * - 支持草稿的查询、创建和状态管理
 * - 实现草稿审核流程的数据访问
 * - 提供分页查询和条件筛选功能
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.repository.article;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.article.ArticleDraft;

@Repository
public interface ArticleDraftRepository extends JpaRepository<ArticleDraft, Long> {
    
    /**
     * 根据审核状态查询草稿列表（分页）
     * @param reviewStatus 审核状态
     * @param pageable 分页参数
     * @return 草稿列表
     */
    Page<ArticleDraft> findByReviewStatus(ArticleDraft.ReviewStatus reviewStatus, Pageable pageable);
    
    /**
     * 查询所有待审核的草稿（分页）
     * @param pageable 分页参数
     * @return 待审核草稿列表
     */
    default Page<ArticleDraft> findPendingDrafts(Pageable pageable) {
        return findByReviewStatus(ArticleDraft.ReviewStatus.PENDING, pageable);
    }
    
    /**
     * 根据编辑者用户ID查询草稿列表（分页）
     * @param editorUserId 编辑者用户ID
     * @param pageable 分页参数
     * @return 用户的草稿列表
     */
    Page<ArticleDraft> findByEditorUserId(Long editorUserId, Pageable pageable);
    
    /**
     * 根据编辑者用户ID和审核状态查询草稿列表
     * @param editorUserId 编辑者用户ID
     * @param reviewStatus 审核状态
     * @param pageable 分页参数
     * @return 用户的指定状态草稿列表
     */
    Page<ArticleDraft> findByEditorUserIdAndReviewStatus(Long editorUserId, 
                                                         ArticleDraft.ReviewStatus reviewStatus, 
                                                         Pageable pageable);
    
    /**
     * 根据关联文章ID查询草稿
     * @param articleId 关联文章ID
     * @return 草稿列表
     */
    List<ArticleDraft> findByArticleId(Long articleId);
    
    /**
     * 根据关联文章ID和审核状态查询草稿
     * @param articleId 关联文章ID
     * @param reviewStatus 审核状态
     * @return 草稿列表
     */
    List<ArticleDraft> findByArticleIdAndReviewStatus(Long articleId, ArticleDraft.ReviewStatus reviewStatus);
    
    /**
     * 查询用户对指定文章是否有待审核的草稿
     * @param articleId 文章ID
     * @param editorUserId 编辑者用户ID
     * @return 待审核草稿（如果存在）
     */
    Optional<ArticleDraft> findByArticleIdAndEditorUserIdAndReviewStatus(Long articleId, 
                                                                         Long editorUserId, 
                                                                         ArticleDraft.ReviewStatus reviewStatus);
    
    /**
     * 查询用户是否有待审核的新建文章草稿
     * @param editorUserId 编辑者用户ID
     * @param title 文章标题
     * @return 待审核草稿（如果存在）
     */
    @Query("SELECT d FROM ArticleDraft d WHERE d.editorUserId = :editorUserId " +
           "AND d.title = :title AND d.articleId IS NULL " +
           "AND d.reviewStatus = 'PENDING'")
    Optional<ArticleDraft> findPendingNewArticleDraft(@Param("editorUserId") Long editorUserId, 
                                                      @Param("title") String title);
    
    /**
     * 统计待审核草稿数量
     * @return 待审核草稿总数
     */
    @Query("SELECT COUNT(d) FROM ArticleDraft d WHERE d.reviewStatus = 'PENDING'")
    long countPendingDrafts();
    
    /**
     * 统计指定用户的草稿数量
     * @param editorUserId 编辑者用户ID
     * @return 用户草稿总数
     */
    long countByEditorUserId(Long editorUserId);
    
    /**
     * 统计指定用户指定状态的草稿数量
     * @param editorUserId 编辑者用户ID
     * @param reviewStatus 审核状态
     * @return 用户指定状态草稿数量
     */
    long countByEditorUserIdAndReviewStatus(Long editorUserId, ArticleDraft.ReviewStatus reviewStatus);
    
    /**
     * 删除指定文章的所有已通过草稿
     * @param articleId 文章ID
     */
    void deleteByArticleIdAndReviewStatus(Long articleId, ArticleDraft.ReviewStatus reviewStatus);
    
    /**
     * 查询最近提交的草稿（用于管理员快速查看）
     * @param pageable 分页参数
     * @return 最近提交的草稿列表
     */
    @Query("SELECT d FROM ArticleDraft d ORDER BY d.submittedAt DESC")
    Page<ArticleDraft> findRecentDrafts(Pageable pageable);
}
