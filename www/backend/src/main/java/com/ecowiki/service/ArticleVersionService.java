package com.ecowiki.service;

import com.ecowiki.entity.ArticleVersion;
import com.ecowiki.entity.ArticleVersionStats;
import com.ecowiki.repository.ArticleVersionRepository;
import com.ecowiki.repository.ArticleVersionStatsRepository;
import com.ecowiki.util.CompressionUtil;
import com.ecowiki.util.DiffUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章版本服务类
 * 实现混合存储策略的文章历史系统
 * 
 * @author EcoWiki
 * @version 1.0
 */
@Service
@Transactional
public class ArticleVersionService {
    
    private static final double DIFF_THRESHOLD = 0.7; // 差异超过70%时存储完整版本
    private static final int OPTIMIZATION_THRESHOLD = 10; // 差异版本超过10个时优化
    private static final int COLD_DATA_DAYS = 30; // 30天未访问视为冷数据
    private static final int ARCHIVE_DAYS = 90; // 90天后归档
    
    @Autowired
    private ArticleVersionRepository versionRepository;
    
    @Autowired
    private ArticleVersionStatsRepository statsRepository;
    
    @Autowired
    private CompressionUtil compressionUtil;
    
    @Autowired
    private DiffUtil diffUtil;
    
    /**
     * 创建文章的新版本
     */
    public ArticleVersion createVersion(Long articleId, String content, String author) {
        try {
            // 获取最新版本号
            Integer nextVersionNumber = getNextVersionNumber(articleId);
            
            // 计算内容哈希
            String contentHash = compressionUtil.calculateHash(content);
            
            // 检查是否有相同内容的版本（去重）
            List<ArticleVersion> existingVersions = versionRepository.findByContentHashAndArticleId(contentHash, articleId);
            if (!existingVersions.isEmpty()) {
                // 返回已存在的版本，不创建重复版本
                return existingVersions.get(0);
            }
            
            // 获取最新版本和基础版本
            Optional<ArticleVersion> latestVersion = versionRepository.findLatestByArticleId(articleId);
            Optional<ArticleVersion> latestBaseVersion = versionRepository.findLatestBaseVersionByArticleId(articleId);
            
            ArticleVersion newVersion;
            
            if (!latestVersion.isPresent() || !latestBaseVersion.isPresent()) {
                // 第一个版本，存储完整内容
                newVersion = createFullVersion(articleId, nextVersionNumber, content, author, contentHash);
            } else {
                // 计算与最新版本的差异
                String latestContent = reconstructContent(latestVersion.get());
                DiffUtil.DiffResult diffResult = diffUtil.calculateDiff(latestContent, content);
                
                if (diffResult.isLargeChange(DIFF_THRESHOLD)) {
                    // 差异太大，存储完整版本
                    newVersion = createFullVersion(articleId, nextVersionNumber, content, author, contentHash);
                } else {
                    // 存储差异版本
                    newVersion = createDiffVersion(articleId, nextVersionNumber, content, author, 
                                                latestContent, latestBaseVersion.get(), contentHash);
                }
            }
            
            // 更新统计信息
            updateVersionStats(articleId, newVersion.getStorageType());
            
            return newVersion;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create article version", e);
        }
    }
    
    /**
     * 获取文章的指定版本内容
     */
    public String getVersionContent(Long articleId, Integer versionNumber) {
        Optional<ArticleVersion> versionOpt = versionRepository.findByArticleIdAndVersionNumber(articleId, versionNumber);
        if (!versionOpt.isPresent()) {
            throw new RuntimeException("Version not found");
        }
        
        ArticleVersion version = versionOpt.get();
        
        // 更新访问统计
        updateAccessStats(articleId);
        
        return reconstructContent(version);
    }
    
    /**
     * 获取文章的最新版本内容
     */
    public String getLatestVersionContent(Long articleId) {
        Optional<ArticleVersion> latestVersion = versionRepository.findLatestByArticleId(articleId);
        if (!latestVersion.isPresent()) {
            throw new RuntimeException("No versions found for article");
        }
        
        updateAccessStats(articleId);
        return reconstructContent(latestVersion.get());
    }
    
    /**
     * 获取文章的版本列表
     */
    public Page<ArticleVersion> getVersionHistory(Long articleId, Pageable pageable) {
        return versionRepository.findByArticleIdOrderByVersionNumberDesc(articleId, pageable);
    }
    
    /**
     * 获取版本统计信息
     */
    public ArticleVersionStats getVersionStats(Long articleId) {
        return statsRepository.findByArticleId(articleId)
                .orElse(new ArticleVersionStats(articleId));
    }
    
    /**
     * 重构指定版本的完整内容
     */
    private String reconstructContent(ArticleVersion version) {
        try {
            if (version.isBaseVersion()) {
                // 基础版本，直接解压内容
                return compressionUtil.decompress(version.getContent(), 
                    version.getCompressionAlgorithm() != null ? version.getCompressionAlgorithm() : "none");
            } else {
                // 差异版本，需要重构
                return reconstructFromDiff(version);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to reconstruct content", e);
        }
    }
    
    /**
     * 从差异版本重构内容
     */
    private String reconstructFromDiff(ArticleVersion diffVersion) throws IOException {
        // 获取基础版本
        ArticleVersion baseVersion = versionRepository.findById(diffVersion.getBaseVersionId())
                .orElseThrow(() -> new RuntimeException("Base version not found"));
        
        String baseContent = compressionUtil.decompress(baseVersion.getContent(),
                baseVersion.getCompressionAlgorithm() != null ? baseVersion.getCompressionAlgorithm() : "none");
        
        // 应用所有从基础版本到目标版本的差异
        List<ArticleVersion> diffVersions = getDiffVersionChain(diffVersion);
        
        String currentContent = baseContent;
        for (ArticleVersion version : diffVersions) {
            String diffData = compressionUtil.decompress(version.getContent(),
                    version.getCompressionAlgorithm() != null ? version.getCompressionAlgorithm() : "none");
            List<DiffUtil.DiffLine> diffLines = diffUtil.decodeDiff(diffData);
            currentContent = diffUtil.applyDiff(currentContent, diffLines);
        }
        
        return currentContent;
    }
    
    /**
     * 获取从基础版本到目标版本的差异链
     */
    private List<ArticleVersion> getDiffVersionChain(ArticleVersion targetVersion) {
        List<ArticleVersion> chain = new ArrayList<>();
        
        if (targetVersion.getStorageType() == ArticleVersion.StorageType.DIFF_FROM_BASE) {
            // 基于基础版本的差异，只需要目标版本
            chain.add(targetVersion);
        } else if (targetVersion.getStorageType() == ArticleVersion.StorageType.DIFF_FROM_PREV) {
            // 基于前一版本的差异，需要重构整个链
            List<ArticleVersion> allVersions = versionRepository.findByArticleIdOrderByVersionNumberDesc(targetVersion.getArticleId());
            
            // 从目标版本向前追溯到基础版本
            ArticleVersion currentVersion = targetVersion;
            while (currentVersion != null && !currentVersion.isBaseVersion()) {
                chain.add(0, currentVersion); // 插入到链的开头
                
                if (currentVersion.getStorageType() == ArticleVersion.StorageType.DIFF_FROM_PREV) {
                    // 查找前一个版本
                    final Integer prevVersionNumber = currentVersion.getVersionNumber() - 1;
                    currentVersion = allVersions.stream()
                            .filter(v -> v.getVersionNumber().equals(prevVersionNumber))
                            .findFirst()
                            .orElse(null);
                } else {
                    break;
                }
            }
        }
        
        return chain;
    }
    
    /**
     * 创建完整版本
     */
    private ArticleVersion createFullVersion(Long articleId, Integer versionNumber, String content, 
                                           String author, String contentHash) throws IOException {
        
        CompressionUtil.CompressionResult compressionResult = compressionUtil.compressBest(content);
        
        ArticleVersion version = new ArticleVersion(articleId, versionNumber, author, 
                ArticleVersion.StorageType.FULL_CONTENT, compressionResult.getCompressedData(), contentHash);
        
        version.setOriginalSize(compressionResult.getOriginalSize());
        version.setCompressedSize(compressionResult.getCompressedSize());
        version.setCompressionAlgorithm(compressionResult.getAlgorithm());
        version.setIsCompressed(true);
        
        return versionRepository.save(version);
    }
    
    /**
     * 创建差异版本
     */
    private ArticleVersion createDiffVersion(Long articleId, Integer versionNumber, String newContent, 
                                           String author, String oldContent, ArticleVersion baseVersion, 
                                           String contentHash) throws IOException {
        
        DiffUtil.DiffResult diffResult = diffUtil.calculateDiff(oldContent, newContent);
        String diffData = diffUtil.encodeDiff(diffResult.getDiffLines());
        
        CompressionUtil.CompressionResult compressionResult = compressionUtil.compressBest(diffData);
        
        ArticleVersion version = new ArticleVersion(articleId, versionNumber, author, 
                ArticleVersion.StorageType.DIFF_FROM_BASE, compressionResult.getCompressedData(), contentHash);
        
        version.setBaseVersionId(baseVersion.getVersionId());
        version.setOriginalSize(compressionResult.getOriginalSize());
        version.setCompressedSize(compressionResult.getCompressedSize());
        version.setCompressionAlgorithm(compressionResult.getAlgorithm());
        version.setIsCompressed(true);
        
        return versionRepository.save(version);
    }
    
    /**
     * 获取下一个版本号
     */
    private Integer getNextVersionNumber(Long articleId) {
        Optional<ArticleVersion> latestVersion = versionRepository.findLatestByArticleId(articleId);
        return latestVersion.map(v -> v.getVersionNumber() + 1).orElse(1);
    }
    
    /**
     * 更新版本统计信息
     */
    private void updateVersionStats(Long articleId, ArticleVersion.StorageType storageType) {
        ArticleVersionStats stats = statsRepository.findByArticleId(articleId)
                .orElse(new ArticleVersionStats(articleId));
        
        stats.incrementVersionCount(storageType);
        
        // 计算总存储大小
        Object[] sizeStats = versionRepository.getStorageSizeStatsByArticleId(articleId);
        if (sizeStats.length >= 2) {
            stats.setTotalStorageSize((Long) sizeStats[0]);
            stats.setCompressedStorageSize((Long) sizeStats[1]);
        }
        
        // 检查是否需要优化
        if (stats.getDiffVersionsCount() > OPTIMIZATION_THRESHOLD) {
            stats.setOptimizationNeeded(true);
        }
        
        statsRepository.save(stats);
    }
    
    /**
     * 更新访问统计
     */
    private void updateAccessStats(Long articleId) {
        ArticleVersionStats stats = statsRepository.findByArticleId(articleId)
                .orElse(new ArticleVersionStats(articleId));
        
        stats.incrementAccessFrequency();
        statsRepository.save(stats);
    }
    
    /**
     * 定时任务：版本优化
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    @Async
    public void optimizeVersions() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<ArticleVersionStats> needOptimization = statsRepository.findArticlesNeedingOptimization(cutoffDate);
        
        for (ArticleVersionStats stats : needOptimization) {
            try {
                optimizeArticleVersions(stats.getArticleId());
                stats.setLastOptimizedAt(LocalDateTime.now());
                stats.setOptimizationNeeded(false);
                statsRepository.save(stats);
            } catch (Exception e) {
                // 记录错误但继续处理其他文章
                System.err.println("Failed to optimize versions for article " + stats.getArticleId() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * 定时任务：冷数据归档
     */
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行
    @Async
    public void archiveColdData() {
        LocalDateTime archiveCutoff = LocalDateTime.now().minusDays(ARCHIVE_DAYS);
        LocalDateTime accessCutoff = LocalDateTime.now().minusDays(COLD_DATA_DAYS);
        
        List<ArticleVersion> coldVersions = versionRepository.findColdVersions(archiveCutoff, accessCutoff);
        
        if (!coldVersions.isEmpty()) {
            // 压缩并归档冷数据
            for (ArticleVersion version : coldVersions) {
                try {
                    archiveVersion(version);
                } catch (Exception e) {
                    System.err.println("Failed to archive version " + version.getVersionId() + ": " + e.getMessage());
                }
            }
            
            // 批量更新归档状态
            List<Long> versionIds = coldVersions.stream()
                    .map(ArticleVersion::getVersionId)
                    .collect(Collectors.toList());
            versionRepository.markVersionsAsArchived(versionIds);
        }
    }
    
    /**
     * 优化文章版本（合并小差异，重新计算基础版本）
     */
    private void optimizeArticleVersions(Long articleId) throws IOException {
        List<ArticleVersion> baseVersions = versionRepository.findBaseVersionsByArticleId(articleId);
        
        for (ArticleVersion baseVersion : baseVersions) {
            List<ArticleVersion> diffVersions = versionRepository.findDiffVersionsAfterBase(articleId, baseVersion.getVersionId());
            
            if (diffVersions.size() > OPTIMIZATION_THRESHOLD) {
                // 重构最新内容
                ArticleVersion latestDiff = diffVersions.get(diffVersions.size() - 1);
                String latestContent = reconstructContent(latestDiff);
                
                // 创建新的基础版本
                String contentHash = compressionUtil.calculateHash(latestContent);
                CompressionUtil.CompressionResult compressionResult = compressionUtil.compressBest(latestContent);
                
                ArticleVersion newBaseVersion = new ArticleVersion(articleId, latestDiff.getVersionNumber(), 
                        latestDiff.getAuthor(), ArticleVersion.StorageType.FULL_CONTENT, 
                        compressionResult.getCompressedData(), contentHash);
                
                newBaseVersion.setOriginalSize(compressionResult.getOriginalSize());
                newBaseVersion.setCompressedSize(compressionResult.getCompressedSize());
                newBaseVersion.setCompressionAlgorithm(compressionResult.getAlgorithm());
                newBaseVersion.setIsCompressed(true);
                
                versionRepository.save(newBaseVersion);
                
                // 删除旧的差异版本
                List<Long> oldDiffIds = diffVersions.stream()
                        .map(ArticleVersion::getVersionId)
                        .collect(Collectors.toList());
                versionRepository.deleteVersionsByIds(oldDiffIds);
            }
        }
    }
    
    /**
     * 归档版本（额外压缩）
     */
    private void archiveVersion(ArticleVersion version) throws IOException {
        if (!version.getIsArchived() && version.getIsCompressed()) {
            // 对已压缩的数据进行额外压缩
            String decompressed = compressionUtil.decompress(version.getContent(), version.getCompressionAlgorithm());
            CompressionUtil.CompressionResult recompressed = compressionUtil.compressBest(decompressed);
            
            // 如果重新压缩效果更好，更新数据
            if (recompressed.getCompressedSize() < version.getCompressedSize()) {
                version.setContent(recompressed.getCompressedData());
                version.setCompressedSize(recompressed.getCompressedSize());
                version.setCompressionAlgorithm(recompressed.getAlgorithm());
            }
        }
        
        version.setIsArchived(true);
        versionRepository.save(version);
    }
    
    /**
     * 恢复文章到指定版本
     * 将指定版本的内容作为新的版本创建
     */
    public ArticleVersion restoreToVersion(Long articleId, Integer versionNumber, String author) {
        // 获取要恢复到的版本内容
        String contentToRestore = getVersionContent(articleId, versionNumber);
        
        // 获取当前最新内容进行比较
        String currentContent;
        try {
            currentContent = getLatestVersionContent(articleId);
        } catch (RuntimeException e) {
            // 如果没有版本，直接创建
            currentContent = "";
        }
        
        // 如果内容相同，不需要恢复
        if (contentToRestore.equals(currentContent)) {
            throw new RuntimeException("Content is the same as current version, no need to restore");
        }
        
        // 创建新版本，内容为恢复的版本内容
        ArticleVersion restoredVersion = createVersion(articleId, contentToRestore, author);
        
        // 设置变更摘要
        restoredVersion.setChangeSummary("恢复到版本 " + versionNumber);
        versionRepository.save(restoredVersion);
        
        return restoredVersion;
    }
}
