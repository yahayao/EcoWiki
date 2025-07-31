package com.ecowiki.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 提交文章草稿请求DTO
 * 
 * 用于用户提交文章草稿时的数据传输。
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2025-07-31
 */
public class SubmitDraftRequest {
    
    /** 关联的文章ID（编辑现有文章时提供，新建文章时为null） */
    private Long articleId;
    
    /** 文章标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;
    
    /** 文章内容 */
    @NotBlank(message = "内容不能为空")
    private String content;
    
    /** 文章分类 */
    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;
    
    /** 文章标签 */
    private String tags;
    
    /** 编辑说明 */
    @Size(max = 500, message = "编辑说明长度不能超过500个字符")
    private String editNotes;
    
    // 构造函数
    public SubmitDraftRequest() {}
    
    public SubmitDraftRequest(Long articleId, String title, String content, 
                             String category, String tags, String editNotes) {
        this.articleId = articleId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.editNotes = editNotes;
    }
    
    // Getter 和 Setter 方法
    public Long getArticleId() {
        return articleId;
    }
    
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public String getEditNotes() {
        return editNotes;
    }
    
    public void setEditNotes(String editNotes) {
        this.editNotes = editNotes;
    }
    
    /**
     * 判断是否为新建文章
     */
    public boolean isNewArticle() {
        return this.articleId == null;
    }
}
