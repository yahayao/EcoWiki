package com.ecowiki.dto.comment;

/**
 * 创建评论请求DTO
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2025-01-21
 */
public class CreateCommentRequest {
    private Long articleId;
    private String content;
    public CreateCommentRequest() {}
    public CreateCommentRequest(Long articleId, String content) {
        this.articleId = articleId;
        this.content = content;
    }

    // Getter方法
    public Long getArticleId() {
        return articleId;
    }

    public String getContent() {
        return content;
    }

    // Setter方法
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

/**
 * 创建回复请求DTO
 */
class CreateReplyRequest {
    private String content;
    public CreateReplyRequest() {}
    public CreateReplyRequest(String content) {
        this.content = content;
    }

    // Getter方法
    public String getContent() {
        return content;
    }

    // Setter方法
    public void setContent(String content) {
        this.content = content;
    }
}
