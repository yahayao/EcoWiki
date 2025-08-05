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
    
    /**
     * 默认构造函数
     */
    public SubmitDraftRequest() {}
    
    /**
     * 全参数构造函数
     * @param articleId 关联的文章ID，新建文章时为null，编辑现有文章时提供具体ID
     * @param title 文章标题，不能为空且长度不超过255个字符
     * @param content 文章内容（Markdown格式），不能为空
     * @param category 文章分类，长度不超过50个字符
     * @param tags 文章标签，多个标签用逗号分隔
     * @param editNotes 编辑说明，用户提交草稿时的备注信息，长度不超过500个字符
     */
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
    
    /**
     * 获取关联的文章ID
     * @return 文章ID，新建文章时为null
     */
    public Long getArticleId() {
        return articleId;
    }
    
    /**
     * 设置关联的文章ID
     * @param articleId 文章ID，新建文章时设为null，编辑现有文章时提供具体ID
     */
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }
    
    /**
     * 获取文章标题
     * @return 文章标题
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * 设置文章标题
     * @param title 文章标题，不能为空且长度不超过255个字符
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * 获取文章内容
     * @return 文章内容（Markdown格式）
     */
    public String getContent() {
        return content;
    }
    
    /**
     * 设置文章内容
     * @param content 文章内容（Markdown格式），不能为空
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * 获取文章分类
     * @return 文章分类
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * 设置文章分类
     * @param category 文章分类，长度不超过50个字符
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * 获取文章标签
     * @return 文章标签，多个标签用逗号分隔
     */
    public String getTags() {
        return tags;
    }
    
    /**
     * 设置文章标签
     * @param tags 文章标签，多个标签用逗号分隔
     */
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    /**
     * 获取编辑说明
     * @return 编辑说明，用户提交草稿时的备注信息
     */
    public String getEditNotes() {
        return editNotes;
    }
    
    /**
     * 设置编辑说明
     * @param editNotes 编辑说明，用户提交草稿时的备注信息，长度不超过500个字符
     */
    public void setEditNotes(String editNotes) {
        this.editNotes = editNotes;
    }
    
    /**
     * 判断是否为新建文章
     * @return true 如果是新建文章（articleId为null），false 如果是编辑现有文章
     */
    public boolean isNewArticle() {
        return this.articleId == null;
    }
}
