package com.ecowiki.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.article.ArticleVersion;

/**
 * 文章版本Repository接口
 * 提供文章版本的数据访问操作
 * 
 * @author EcoWiki
 * @version 1.0
 */
@Repository
public interface ArticleVersionRepository extends JpaRepository<ArticleVersion, Long> {
    
    /**
     * 根据文章ID查找所有版本
     */
    List<ArticleVersion> findByArticleIdOrderByVersionNumberDesc(Long articleId);
    
    /**
     * 根据文章ID分页查找版本
     */
    Page<ArticleVersion> findByArticleIdOrderByVersionNumberDesc(Long articleId, Pageable pageable);
    
    /**
     * 根据文章ID和版本号查找特定版本
     */
    Optional<ArticleVersion> findByArticleIdAndVersionNumber(Long articleId, Integer versionNumber);
    
    /**
     * 查找文章的最新版本
     */
    @Query("SELECT v FROM ArticleVersion v WHERE v.articleId = :articleId ORDER BY v.versionNumber DESC LIMIT 1")
    Optional<ArticleVersion> findLatestByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 查找文章的最新基础版本（完整内容）
     */
    @Query("SELECT v FROM ArticleVersion v WHERE v.articleId = :articleId AND v.storageType = 'FULL_CONTENT' ORDER BY v.versionNumber DESC LIMIT 1")
    Optional<ArticleVersion> findLatestBaseVersionByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 查找指定文章的所有基础版本
     */
    @Query("SELECT v FROM ArticleVersion v WHERE v.articleId = :articleId AND v.storageType = 'FULL_CONTENT' ORDER BY v.versionNumber ASC")
    List<ArticleVersion> findBaseVersionsByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 查找指定基础版本之后的所有差异版本
     */
    @Query("SELECT v FROM ArticleVersion v WHERE v.articleId = :articleId AND v.baseVersionId = :baseVersionId ORDER BY v.versionNumber ASC")
    List<ArticleVersion> findDiffVersionsAfterBase(@Param("articleId") Long articleId, @Param("baseVersionId") Long baseVersionId);
    
    /**
     * 查找需要归档的版本（超过指定时间且未归档）
     */
    @Query("SELECT v FROM ArticleVersion v WHERE v.createdAt < :cutoffDate AND v.isArchived = false")
    List<ArticleVersion> findVersionsToArchive(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 查找需要优化的文章（有很多小差异版本）
     */
    @Query("SELECT v.articleId, COUNT(v) as diffCount FROM ArticleVersion v " +
           "WHERE v.storageType IN ('DIFF_FROM_BASE', 'DIFF_FROM_PREV') " +
           "AND v.baseVersionId = :baseVersionId " +
           "GROUP BY v.articleId " +
           "HAVING COUNT(v) > :threshold")
    List<Object[]> findArticlesNeedingOptimization(@Param("baseVersionId") Long baseVersionId, @Param("threshold") Integer threshold);
    
    /**
     * 查找特定文章的版本统计信息
     */
    @Query("SELECT " +
           "COUNT(v) as totalVersions, " +
           "SUM(CASE WHEN v.storageType = 'FULL_CONTENT' THEN 1 ELSE 0 END) as baseVersions, " +
           "SUM(CASE WHEN v.storageType != 'FULL_CONTENT' THEN 1 ELSE 0 END) as diffVersions, " +
           "SUM(CASE WHEN v.isArchived = true THEN 1 ELSE 0 END) as archivedVersions " +
           "FROM ArticleVersion v WHERE v.articleId = :articleId")
    Object[] getVersionStatsByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 查找存储大小统计
     */
    @Query("SELECT " +
           "SUM(CASE WHEN v.originalSize IS NOT NULL THEN v.originalSize ELSE 0 END) as totalOriginalSize, " +
           "SUM(CASE WHEN v.compressedSize IS NOT NULL THEN v.compressedSize ELSE 0 END) as totalCompressedSize " +
           "FROM ArticleVersion v WHERE v.articleId = :articleId")
    Object[] getStorageSizeStatsByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 批量更新版本为已归档状态
     */
    @Modifying
    @Query("UPDATE ArticleVersion v SET v.isArchived = true WHERE v.versionId IN :versionIds")
    int markVersionsAsArchived(@Param("versionIds") List<Long> versionIds);
    
    /**
     * 删除指定版本
     */
    @Modifying
    @Query("DELETE FROM ArticleVersion v WHERE v.versionId IN :versionIds")
    int deleteVersionsByIds(@Param("versionIds") List<Long> versionIds);
    
    /**
     * 获取文章的版本数量
     */
    @Query("SELECT COUNT(v) FROM ArticleVersion v WHERE v.articleId = :articleId")
    long countVersionsByArticleId(@Param("articleId") Long articleId);
    
    /**
     * 查找过期未访问的版本（用于冷数据处理）
     */
    @Query("SELECT v FROM ArticleVersion v WHERE v.createdAt < :cutoffDate AND v.isArchived = false " +
           "AND NOT EXISTS (SELECT 1 FROM ArticleVersionStats s WHERE s.articleId = v.articleId AND s.lastAccessedAt > :accessCutoff)")
    List<ArticleVersion> findColdVersions(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("accessCutoff") LocalDateTime accessCutoff);
    
    /**
     * 查找指定哈希值的版本（用于去重）
     */
    @Query("SELECT v FROM ArticleVersion v WHERE v.contentHash = :hash AND v.articleId = :articleId")
    List<ArticleVersion> findByContentHashAndArticleId(@Param("hash") String hash, @Param("articleId") Long articleId);
}
