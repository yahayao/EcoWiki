package com.ecowiki.dto;

public class ArticleStatisticsDto {
    
    private long totalArticles;
    private Long totalViews;
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
