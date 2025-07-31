package com.ecowiki.dto.article;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 审核草稿请求DTO
 * 
 * 用于superadmin审核文章草稿时的数据传输。
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2025-07-31
 */
public class ReviewDraftRequest {
    
    /** 审核结果：true为通过，false为拒绝 */
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;
    
    /** 审核备注 */
    @Size(max = 1000, message = "审核备注长度不能超过1000个字符")
    private String reviewNotes;
    
    // 构造函数
    public ReviewDraftRequest() {}
    
    public ReviewDraftRequest(Boolean approved, String reviewNotes) {
        this.approved = approved;
        this.reviewNotes = reviewNotes;
    }
    
    // Getter 和 Setter 方法
    public Boolean getApproved() {
        return approved;
    }
    
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
    
    public String getReviewNotes() {
        return reviewNotes;
    }
    
    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
    
    /**
     * 判断是否通过审核
     */
    public boolean isApproved() {
        return approved != null && approved;
    }
    
    /**
     * 判断是否拒绝审核
     */
    public boolean isRejected() {
        return approved != null && !approved;
    }
}
