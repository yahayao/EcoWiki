package com.ecowiki.repository;

import com.ecowiki.entity.ArticleVersionStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文章版本统计Repository接口
 * 
 * @author EcoWiki
 * @version 1.0
 */
@Repository
public interface ArticleVersionStatsRepository extends JpaRepository<ArticleVersionStats, Long> {
    
    /**
     * 根据文章ID查找统计信息
     */
    Optional<ArticleVersionStats> findByArticleId(Long articleId);
    
    /**
     * 查找需要优化的文章统计
     */
    @Query("SELECT s FROM ArticleVersionStats s WHERE s.optimizationNeeded = true OR s.lastOptimizedAt < :cutoffDate OR s.lastOptimizedAt IS NULL")
    List<ArticleVersionStats> findArticlesNeedingOptimization(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 查找访问频率低的文章（用于冷数据识别）
     */
    @Query("SELECT s FROM ArticleVersionStats s WHERE s.accessFrequency < :threshold AND s.lastAccessedAt < :cutoffDate")
    List<ArticleVersionStats> findLowAccessArticles(@Param("threshold") Integer threshold, @Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 查找存储占用最大的文章
     */
    @Query("SELECT s FROM ArticleVersionStats s ORDER BY s.totalStorageSize DESC")
    List<ArticleVersionStats> findByStorageSizeDesc();
    
    /**
     * 查找压缩率最低的文章
     */
    @Query("SELECT s FROM ArticleVersionStats s WHERE s.compressedStorageSize > 0 ORDER BY (s.compressedStorageSize * 1.0 / s.totalStorageSize) DESC")
    List<ArticleVersionStats> findByCompressionRatioDesc();
}
