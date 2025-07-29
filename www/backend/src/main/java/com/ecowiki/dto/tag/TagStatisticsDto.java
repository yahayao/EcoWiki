package com.ecowiki.dto.tag;

import java.util.List;

/**
 * 标签统计数据传输对象
 * 
 * 用于传递标签相关的统计信息，包括总数、未使用标签数量、
 * 使用频率统计等数据。
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
public class TagStatisticsDto {
    private Long totalTags;
    private Long unusedTagCount;
    private List<Object[]> usageStatistics;
    public TagStatisticsDto() {}
    public TagStatisticsDto(Long totalTags, Long unusedTagCount, List<Object[]> usageStatistics) {
        this.totalTags = totalTags;
        this.unusedTagCount = unusedTagCount;
        this.usageStatistics = usageStatistics;
    }

    // ==================== Getter和Setter方法 ====================
    
    public Long getTotalTags() {
        return totalTags;
    }
    
    public void setTotalTags(Long totalTags) {
        this.totalTags = totalTags;
    }
    
    public Long getUnusedTagCount() {
        return unusedTagCount;
    }
    
    public void setUnusedTagCount(Long unusedTagCount) {
        this.unusedTagCount = unusedTagCount;
    }
    
    public List<Object[]> getUsageStatistics() {
        return usageStatistics;
    }
    
    public void setUsageStatistics(List<Object[]> usageStatistics) {
        this.usageStatistics = usageStatistics;
    }
    
    @Override
    public String toString() {
        return "TagStatisticsDto{" +
                "totalTags=" + totalTags +
                ", unusedTagCount=" + unusedTagCount +
                ", usageStatistics=" + usageStatistics +
                '}';
    }
}
