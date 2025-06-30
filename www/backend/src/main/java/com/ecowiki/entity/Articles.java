package com.ecowiki.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "articles")
public class Articles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long articleId;

    @Column(unique = true, nullable = false)
    private String title; // 文章标题

    @Column(nullable = false)
    private String content; // 文章内容

    @Column(nullable = false)
    private String author; // 作者

    @Column(name = "publish_date",updatable = false)
    private LocalDateTime publishDate; // 创建时间

    @Column(name = "update_time")
    private LocalDateTime updateTime; // 最后修改时间

    private String category; // 文章分类
    private String tags; // 文章标签

    @Column(name = "views")
    private Integer viewCount = 0; // 浏览次数

    @Column(name = "likes")
    private Integer likeCount = 0; // 点赞次数

    @Column(name = "comments")
    private Integer commentCount = 0; // 评论次数

    // 默认构造函数
    public Articles() {}

    public Articles(String title,String content,String author,String category,String tags){
        this.title = title;
        this.content = content;
        this.author = author;
        this.category = category;
        this.tags = tags;
        this.publishDate = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    // Getters and Setters
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
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }   
    public LocalDateTime getPublishDate() {
        return publishDate;
    }
    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
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
    public Integer getViewCount() {
        return viewCount;
    }
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    public Integer getLikeCount() {
        return likeCount;
    }
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    public Integer getCommentCount() {
        return commentCount;
    }
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }
    /**
     * 重写toString方法，便于调试和日志记录
     * 在调试过程中，可以直接打印对象，查看其所有属性的值
     * 例如：
     * Article article = new Article();
     * System.out.println(article.toString());
     * @return 字符串表示形式
     * @author EcoWiki
     * @date 2023/10/20
     * * @version 1.0
     * @since 1.0
     */
    @Override
    public String toString() {
        return "Articles{" +
                "articleId=" + articleId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", author='" + author + '\'' +
                ", publishDate=" + publishDate +
                ", updateTime=" + updateTime +
                ", category='" + category + '\'' +
                ", tags='" + tags + '\'' +
                ", viewCount=" + viewCount +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                '}';
    }
}
    
