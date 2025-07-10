package com.ecowiki.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 文章版本实体类
 * 支持混合存储策略：
 * - 第一个版本存储完整内容
 * - 小改动只存储差异
 * - 差异大小超过原文70%时，直接存储完整版本
 * - 冷热数据分离：旧版本移到压缩存储
 * 
 * @author EcoWiki
 * @version 1.0
 */
@Entity
@Table(name = "article_versions", indexes = {
    @Index(name = "idx_article_id", columnList = "article_id"),
    @Index(name = "idx_version_number", columnList = "version_number"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_storage_type", columnList = "storage_type"),
    @Index(name = "idx_is_archived", columnList = "is_archived")
})
public class ArticleVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private Long versionId;
    
    @NotNull
    @Column(name = "article_id", nullable = false)
    private Long articleId;
    
    @NotNull
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;
    
    @Size(max = 100)
    @Column(name = "author", length = 100)
    private String author;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false, length = 20)
    private StorageType storageType;
    
    @Lob
    @Column(name = "content", columnDefinition = "LONGBLOB")
    private byte[] content;
    
    @Column(name = "content_hash", length = 64)
    private String contentHash;
    
    @Column(name = "original_size")
    private Long originalSize;
    
    @Column(name = "compressed_size")
    private Long compressedSize;
    
    @Column(name = "base_version_id")
    private Long baseVersionId;
    
    @Column(name = "is_compressed", nullable = false)
    private Boolean isCompressed = false;
    
    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;
    
    @Size(max = 500)
    @Column(name = "change_summary", length = 500)
    private String changeSummary;
    
    @Column(name = "compression_algorithm", length = 20)
    private String compressionAlgorithm;
    
    public enum StorageType {
        FULL_CONTENT,    // 完整内容
        DIFF_FROM_BASE,  // 基于基础版本的差异
        DIFF_FROM_PREV   // 基于前一版本的差异
    }
    
    // 构造函数
    public ArticleVersion() {
        this.createdAt = LocalDateTime.now();
    }
    
    public ArticleVersion(Long articleId, Integer versionNumber, String author, 
                         StorageType storageType, byte[] content, String contentHash) {
        this.articleId = articleId;
        this.versionNumber = versionNumber;
        this.author = author;
        this.storageType = storageType;
        this.content = content;
        this.contentHash = contentHash;
        this.createdAt = LocalDateTime.now();
        this.isCompressed = false;
        this.isArchived = false;
    }
    
    // Getters and Setters
    public Long getVersionId() {
        return versionId;
    }
    
    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }
    
    public Long getArticleId() {
        return articleId;
    }
    
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    
    public Integer getVersionNumber() {
        return versionNumber;
    }
    
    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public StorageType getStorageType() {
        return storageType;
    }
    
    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }
    
    public byte[] getContent() {
        return content;
    }
    
    public void setContent(byte[] content) {
        this.content = content;
    }
    
    public String getContentHash() {
        return contentHash;
    }
    
    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }
    
    public Long getOriginalSize() {
        return originalSize;
    }
    
    public void setOriginalSize(Long originalSize) {
        this.originalSize = originalSize;
    }
    
    public Long getCompressedSize() {
        return compressedSize;
    }
    
    public void setCompressedSize(Long compressedSize) {
        this.compressedSize = compressedSize;
    }
    
    public Long getBaseVersionId() {
        return baseVersionId;
    }
    
    public void setBaseVersionId(Long baseVersionId) {
        this.baseVersionId = baseVersionId;
    }
    
    public Boolean getIsCompressed() {
        return isCompressed;
    }
    
    public void setIsCompressed(Boolean isCompressed) {
        this.isCompressed = isCompressed;
    }
    
    public Boolean getIsArchived() {
        return isArchived;
    }
    
    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }
    
    public String getChangeSummary() {
        return changeSummary;
    }
    
    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }
    
    public String getCompressionAlgorithm() {
        return compressionAlgorithm;
    }
    
    public void setCompressionAlgorithm(String compressionAlgorithm) {
        this.compressionAlgorithm = compressionAlgorithm;
    }
    
    /**
     * 判断是否为基础版本（完整内容）
     */
    public boolean isBaseVersion() {
        return storageType == StorageType.FULL_CONTENT;
    }
    
    /**
     * 判断是否为差异版本
     */
    public boolean isDiffVersion() {
        return storageType == StorageType.DIFF_FROM_BASE || storageType == StorageType.DIFF_FROM_PREV;
    }
    
    /**
     * 计算压缩率
     */
    public double getCompressionRatio() {
        if (originalSize == null || compressedSize == null || originalSize == 0) {
            return 0.0;
        }
        return (double) compressedSize / originalSize;
    }
}
