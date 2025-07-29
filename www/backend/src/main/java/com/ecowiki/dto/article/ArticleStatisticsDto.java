package com.ecowiki.dto.article;

/**
 * 文章统计数据DTO
 * <p>
 * 用于传输文章相关的统计信息，包括文章总数、总浏览量、总点赞数等。
 * 主要用于管理后台的统计展示和数据分析。
 * <p>
 * <b>设计说明：</b>
 * - 包含核心统计指标
 * - 处理空值情况，默认为0
 * - 便于扩展其他统计字段
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
public class ArticleStatisticsDto {
    /** 文章总数 */
    private long totalArticles;
    /** 总浏览量 */
    private Long totalViews;
    /** 总点赞数 */
    private Long totalLikes;
    
    // 构造函数
    public ArticleStatisticsDto() {}
    
    public ArticleStatisticsDto(long totalArticles, Long totalViews, Long totalLikes) {
        this.totalArticles = totalArticles;
        this.totalViews = totalViews != null ? totalViews : 0L;
        this.totalLikes = totalLikes != null ? totalLikes : 0L;
    }
    
    // Getters and Setters
    public long getTotalArticles() {
        return totalArticles;
    }
    
    public void setTotalArticles(long totalArticles) {
        this.totalArticles = totalArticles;
    }
    
    public Long getTotalViews() {
        return totalViews;
    }
    
    public void setTotalViews(Long totalViews) {
        this.totalViews = totalViews;
    }
    
    public Long getTotalLikes() {
        return totalLikes;
    }
    
    public void setTotalLikes(Long totalLikes) {
        this.totalLikes = totalLikes;
    }
}
