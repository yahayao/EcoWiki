/**
 * 创建评论请求DTO类
 * 
 * 功能包括：
 * - 评论创建请求数据封装
 * - 文章评论信息传输
 * - 评论内容验证支持
 * - 用户评论操作处理
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.dto.comment;

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
