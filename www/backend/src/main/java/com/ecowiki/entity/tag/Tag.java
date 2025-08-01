package com.ecowiki.entity.tag;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.ecowiki.entity.article.Article;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 标签实体类
 * 
 * 用于存储文章标签信息的独立实体。
 * 通过多对多关系与文章实体建立关联，支持标签的统一管理和重用。
 * 
 * 主要功能：
 * - 标签名称管理：存储唯一的标签名称
 * - 标签创建时间：记录标签首次创建的时间
 * - 标签使用统计：通过关联文章数量统计标签使用频率
 * - 标签描述：可选的标签描述信息
 * 
 * 数据库表结构：
 * - tag_id: 标签唯一标识符（主键，自增）
 * - tag_name: 标签名称（唯一，必填）
 * - description: 标签描述（可选）
 * - created_time: 创建时间（自动设置）
 * 
 * 关联关系：
 * - 与Article实体建立多对多关系（一个标签可以关联多篇文章，一篇文章可以有多个标签）
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "tags")
public class Tag {
    
    /**
     * 标签唯一标识符
     * 数据库主键，自动递增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;
    
    /**
     * 标签名称
     * 必须唯一且不能为空，用于标签的展示和搜索
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    @Column(name = "tag_name", unique = true, nullable = false, length = 50)
    private String tagName;
    
    /**
     * 标签描述
     * 可选字段，用于详细描述标签的含义和用途
     */
    @Size(max = 255, message = "标签描述长度不能超过255个字符")
    @Column(name = "description")
    private String description;
    
    /**
     * 创建时间
     * 标签首次创建的时间戳
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    /**
     * 关联的文章集合
     * 多对多关系，mappedBy指向Article实体中的tags字段
     */
    @ManyToMany(mappedBy = "tags")
    private Set<Article> articles = new HashSet<>();
    
    // ==================== 构造函数 ====================
    
    /**
     * 默认构造函数
     * JPA要求实体类必须有无参构造函数
     */
    public Tag() {}
    
    /**
     * 便捷构造函数
     * 创建标签并自动设置创建时间
     * 
     * @param tagName 标签名称
     */
    public Tag(String tagName) {
        this.tagName = tagName;
        this.createdTime = LocalDateTime.now();
    }
    
    /**
     * 完整构造函数
     * 创建标签并设置所有信息
     * 
     * @param tagName 标签名称
     * @param description 标签描述
     */
    public Tag(String tagName, String description) {
        this.tagName = tagName;
        this.description = description;
        this.createdTime = LocalDateTime.now();
    }
    
    // ==================== Getter和Setter方法 ====================
    
    /**
     * 获取标签ID
     * @return 标签唯一标识符
     */
    public Long getTagId() {
        return tagId;
    }
    
    /**
     * 设置标签ID
     * @param tagId 标签唯一标识符
     */
    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
    
    /**
     * 获取标签名称
     * @return 标签名称
     */
    public String getTagName() {
        return tagName;
    }
    
    /**
     * 设置标签名称
     * @param tagName 标签名称，不能为空且必须唯一
     */
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    
    /**
     * 获取标签描述
     * @return 标签描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 设置标签描述
     * @param description 标签描述
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * 获取创建时间
     * @return 标签创建时间
     */
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
    /**
     * 设置创建时间
     * @param createdTime 标签创建时间
     */
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
    
    /**
     * 获取关联的文章集合
     * @return 关联的文章集合
     */
    public Set<Article> getArticles() {
        return articles;
    }
    
    /**
     * 设置关联的文章集合
     * @param articles 关联的文章集合
     */
    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }
    
    // ==================== 业务方法 ====================
    
    /**
     * 添加关联文章
     * @param article 要关联的文章
     */
    public void addArticle(Article article) {
        this.articles.add(article);
        article.getTags().add(this);
    }
    
    /**
     * 移除关联文章
     * @param article 要移除关联的文章
     */
    public void removeArticle(Article article) {
        this.articles.remove(article);
        article.getTags().remove(this);
    }
    
    /**
     * 获取使用此标签的文章数量
     * @return 文章数量
     */
    public int getArticleCount() {
        return articles.size();
    }
    
    // ==================== Object方法重写 ====================
    
    /**
     * 重写toString方法，便于调试和日志记录
     */
    @Override
    public String toString() {
        return "Tag{" +
                "tagId=" + tagId +
                ", tagName='" + tagName + '\'' +
                ", description='" + description + '\'' +
                ", createdTime=" + createdTime +
                ", articleCount=" + getArticleCount() +
                '}';
    }
    
    /**
     * 重写equals方法，基于标签名称判断相等性
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return tagName != null && tagName.equals(tag.tagName);
    }
    
    /**
     * 重写hashCode方法，基于标签名称计算哈希值
     */
    @Override
    public int hashCode() {
        return tagName != null ? tagName.hashCode() : 0;
    }
}
