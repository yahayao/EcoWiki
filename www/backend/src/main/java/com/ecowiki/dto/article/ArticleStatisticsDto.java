/**
 * 文章统计数据DTO类
 * 
 * 功能：
 * - 传输文章相关的统计信息
 * - 支持文章总数、总浏览量、总点赞数统计
 * - 自动处理空值情况（默认为0）
 * - 便于扩展其他统计字段
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.dto.article;
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
