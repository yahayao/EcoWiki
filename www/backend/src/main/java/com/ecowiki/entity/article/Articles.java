package com.ecowiki.entity.article;

import java.time.LocalDateTime;

import com.ecowiki.entity.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 文章实体类
 * 
 * 该实体类映射数据库中的articles表，是EcoWiki系统的核心业务实体。
 * 负责存储和管理Wiki文章的完整信息，包括内容、元数据和统计数据。
 * 
 * 主要功能：
 * - 文章内容管理：存储文章标题、内容、作者等基本信息
 * - 分类标签系统：支持文章分类和自定义标签管理
 * - 时间追踪：记录文章的发布时间和最后更新时间
 * - 统计数据：跟踪浏览量、点赞数、评论数等互动指标
 * - 内容版本控制：支持文章内容的修改和历史追踪
 * 
 * 数据库表结构：
 * - article_id: 文章唯一标识符（主键，自增）
 * - title: 文章标题（唯一，必填）
 * - content: 文章内容（必填，支持Wiki语法）
 * - author: 文章作者（必填）
 * - publish_date: 发布时间（创建时自动设置，不可更新）
 * - update_time: 最后更新时间（修改时自动更新）
 * - category: 文章分类（可选）
 * - tags: 文章标签（可选，逗号分隔）
 * - views: 浏览次数（默认0）
 * - likes: 点赞次数（默认0）
 * - comments: 评论次数（默认0）
 * 
 * 业务特性：
 * - 内容格式：支持Wiki语法、Markdown等格式
 * - 分类体系：灵活的分类标签系统
 * - 社交功能：浏览、点赞、评论等互动功能
 * - 搜索优化：标题和内容支持全文搜索
 * - 权限控制：结合用户权限控制文章的查看和编辑
 * 
 * 使用场景：
 * - 知识库构建：企业或个人知识库的核心内容载体
 * - 协作编辑：多用户协作编辑和维护文章内容
 * - 内容管理：文章的创建、编辑、发布、归档管理
 * - 数据分析：基于统计数据分析内容受欢迎程度
 * 
 * 扩展性设计：
 * - 版本控制：可扩展支持文章版本历史
 * - 附件管理：可关联文件、图片等附件资源
 * - 评论系统：可扩展评论和回复功能
 * - 工作流：可集成审核、发布工作流
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 * @see User 用户实体类（作者关联）
 */
@Entity
@Table(name = "articles")
public class Articles {
    
    /**
     * 文章唯一标识符
     * 数据库主键，自动递增
     * 用于唯一标识每篇文章
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long articleId;

    /**
     * 文章标题
     * 文章的主要标识和显示名称
     * 必须唯一且不能为空，用于SEO和搜索优化
     */
    @Column(unique = true, nullable = false)
    private String title;

    /**
     * 文章内容
     * 文章的主体内容，支持Wiki语法和富文本格式
     * 不能为空，是文章的核心数据
     */
    @Column(nullable = false)
    private String content;

    /**
     * 文章作者
     * 文章的创建者或主要贡献者
     * 必填字段，用于版权和责任追溯
     */
    @Column(nullable = false)
    private String author;

    /**
     * 文章发布时间
     * 文章首次创建的时间戳
     * 设置为不可更新，确保发布时间的准确性
     */
    @Column(name = "publish_date", updatable = false)
    private LocalDateTime publishDate;

    /**
     * 最后更新时间
     * 文章内容最后一次修改的时间戳
     * 用于显示文章的最新程度和版本控制
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 文章分类
     * 文章所属的主要分类，用于内容组织和导航
     * 可选字段，支持分层分类结构
     */
    private String category;

    /**
     * 文章标签
     * 文章的关键词标签，多个标签用逗号分隔
     * 用于内容标记、搜索和相关推荐
     */
    private String tags;

    /**
     * 浏览次数
     * 文章被查看的总次数
     * 默认值为0，用于热度统计和排序
     */
    @Column(name = "views")
    private Integer viewCount = 0;

    /**
     * 点赞次数
     * 文章获得的点赞总数
     * 默认值为0，用于质量评估和推荐算法
     */
    @Column(name = "likes")
    private Integer likeCount = 0;

    /**
     * 评论次数
     * 文章下的评论总数
     * 默认值为0，反映文章的互动活跃度
     */
    @Column(name = "comments")
    private Integer commentCount = 0;

    // ==================== 构造函数 ====================
    
    /**
     * 默认构造函数
     * JPA要求实体类必须有无参构造函数
     */
    public Articles() {}

    /**
     * 便捷构造函数
     * 用于快速创建文章实例，自动设置创建和更新时间
     * 
     * @param title 文章标题
     * @param content 文章内容
     * @param author 文章作者
     * @param category 文章分类
     * @param tags 文章标签
     */
    public Articles(String title, String content, String author, String category, String tags) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.category = category;
        this.tags = tags;
        this.publishDate = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    // ==================== Getter和Setter方法 ====================
    
    /**
     * 获取文章ID
     * @return 文章唯一标识符
     */
    public Long getArticleId() {
        return articleId;
    }
    
    /**
     * 设置文章ID
     * @param articleId 文章唯一标识符
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
     * @param title 文章标题，不能为空且必须唯一
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * 获取文章内容
     * @return 文章内容
     */
    public String getContent() {
        return content;
    }
    
    /**
     * 设置文章内容
     * @param content 文章内容，不能为空
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * 获取文章作者
     * @return 文章作者
     */
    /**
     * 获取文章作者
     * @return 文章作者
     */
    public String getAuthor() {
        return author;
    }
    
    /**
     * 设置文章作者
     * @param author 文章作者，不能为空
     */
    public void setAuthor(String author) {
        this.author = author;
    }
    
    /**
     * 获取发布时间
     * @return 文章发布时间
     */
    public LocalDateTime getPublishDate() {
        return publishDate;
    }
    
    /**
     * 设置发布时间
     * @param publishDate 文章发布时间
     */
    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }
    
    /**
     * 获取更新时间
     * @return 文章最后更新时间
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    /**
     * 设置更新时间
     * @param updateTime 文章最后更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
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
     * @param category 文章分类
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * 获取文章标签
     * @return 文章标签（逗号分隔）
     */
    public String getTags() {
        return tags;
    }
    
    /**
     * 设置文章标签
     * @param tags 文章标签（多个标签用逗号分隔）
     */
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    /**
     * 获取浏览次数
     * @return 文章浏览次数
     */
    public Integer getViewCount() {
        return viewCount;
    }
    
    /**
     * 设置浏览次数
     * @param viewCount 文章浏览次数
     */
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    /**
     * 获取点赞次数
     * @return 文章点赞次数
     */
    public Integer getLikeCount() {
        return likeCount;
    }
    
    /**
     * 设置点赞次数
     * @param likeCount 文章点赞次数
     */
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    /**
     * 获取评论次数
     * @return 文章评论次数
     */
    public Integer getCommentCount() {
        return commentCount;
    }
    
    /**
     * 设置评论次数
     * @param commentCount 文章评论次数
     */
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }
    
    /**
     * 重写toString方法，便于调试和日志记录
     * 
     * 提供对象的字符串表示形式，包含所有重要属性信息。
     * 在调试过程中可以直接打印对象，查看其所有属性的值。
     * 
     * 使用示例：
     * Articles article = new Articles();
     * System.out.println(article.toString());
     * 
     * @return 包含所有属性信息的字符串表示形式
     * @author EcoWiki开发团队
     * @version 1.0.0
     * @since 2024-01-01
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
    
