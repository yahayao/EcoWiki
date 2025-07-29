package com.ecowiki.entity.article;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 文章版本统计信息实体类
 * 用于跟踪文章版本的统计数据和优化信息
 * 
 * @author EcoWiki
 * @version 1.0
 */
@Entity
@Table(name = "article_version_stats", indexes = {
    @Index(name = "idx_article_id", columnList = "article_id"),
    @Index(name = "idx_last_optimized", columnList = "last_optimized_at"),
    @Index(name = "idx_last_accessed", columnList = "last_accessed_at")
})
public class ArticleVersionStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stats_id")
    private Long statsId;
    
    @Column(name = "article_id", nullable = false, unique = true)
    private Long articleId;
    
    @Column(name = "total_versions", nullable = false)
    private Integer totalVersions = 0;
    
    @Column(name = "base_versions_count", nullable = false)
    private Integer baseVersionsCount = 0;
    
    @Column(name = "diff_versions_count", nullable = false)
    private Integer diffVersionsCount = 0;
    
    @Column(name = "archived_versions_count", nullable = false)
    private Integer archivedVersionsCount = 0;
    
    @Column(name = "total_storage_size")
    private Long totalStorageSize = 0L;
    
    @Column(name = "compressed_storage_size")
    private Long compressedStorageSize = 0L;
    
    @Column(name = "last_optimized_at")
    private LocalDateTime lastOptimizedAt;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    @Column(name = "access_frequency", nullable = false)
    private Integer accessFrequency = 0;
    
    @Column(name = "optimization_needed", nullable = false)
    private Boolean optimizationNeeded = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public ArticleVersionStats() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ArticleVersionStats(Long articleId) {
        this.articleId = articleId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getStatsId() {
        return statsId;
    }
    
    public void setStatsId(Long statsId) {
        this.statsId = statsId;
    }
    
    public Long getArticleId() {
        return articleId;
    }
    
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    
    public Integer getTotalVersions() {
        return totalVersions;
    }
    
    public void setTotalVersions(Integer totalVersions) {
        this.totalVersions = totalVersions;
    }
    
    public Integer getBaseVersionsCount() {
        return baseVersionsCount;
    }
    
    public void setBaseVersionsCount(Integer baseVersionsCount) {
        this.baseVersionsCount = baseVersionsCount;
    }
    
    public Integer getDiffVersionsCount() {
        return diffVersionsCount;
    }
    
    public void setDiffVersionsCount(Integer diffVersionsCount) {
        this.diffVersionsCount = diffVersionsCount;
    }
    
    public Integer getArchivedVersionsCount() {
        return archivedVersionsCount;
    }
    
    public void setArchivedVersionsCount(Integer archivedVersionsCount) {
        this.archivedVersionsCount = archivedVersionsCount;
    }
    
    public Long getTotalStorageSize() {
        return totalStorageSize;
    }
    
    public void setTotalStorageSize(Long totalStorageSize) {
        this.totalStorageSize = totalStorageSize;
    }
    
    public Long getCompressedStorageSize() {
        return compressedStorageSize;
    }
    
    public void setCompressedStorageSize(Long compressedStorageSize) {
        this.compressedStorageSize = compressedStorageSize;
    }
    
    public LocalDateTime getLastOptimizedAt() {
        return lastOptimizedAt;
    }
    
    public void setLastOptimizedAt(LocalDateTime lastOptimizedAt) {
        this.lastOptimizedAt = lastOptimizedAt;
    }
    
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
    
    public Integer getAccessFrequency() {
        return accessFrequency;
    }
    
    public void setAccessFrequency(Integer accessFrequency) {
        this.accessFrequency = accessFrequency;
    }
    
    public Boolean getOptimizationNeeded() {
        return optimizationNeeded;
    }
    
    public void setOptimizationNeeded(Boolean optimizationNeeded) {
        this.optimizationNeeded = optimizationNeeded;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 增加版本计数
     */
    public void incrementVersionCount(ArticleVersion.StorageType storageType) {
        this.totalVersions++;
        if (storageType == ArticleVersion.StorageType.FULL_CONTENT) {
            this.baseVersionsCount++;
        } else {
            this.diffVersionsCount++;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加访问频率
     */
    public void incrementAccessFrequency() {
        this.accessFrequency++;
        this.lastAccessedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 计算压缩率
     */
    public double getCompressionRatio() {
        if (totalStorageSize == null || compressedStorageSize == null || totalStorageSize == 0) {
            return 0.0;
        }
        return (double) compressedStorageSize / totalStorageSize;
    }
    
    /**
     * 判断是否需要优化
     */
    public boolean needsOptimization() {
        // 如果最后一次优化超过30天或从未优化过
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return lastOptimizedAt == null || lastOptimizedAt.isBefore(thirtyDaysAgo);
    }
}
