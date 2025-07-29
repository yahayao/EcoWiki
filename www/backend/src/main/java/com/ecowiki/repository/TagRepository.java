package com.ecowiki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.tag.Tag;

/**
 * 标签数据访问接口
 * 
 * 提供标签实体的数据库操作方法，包括基本的CRUD操作和自定义查询。
 * 支持标签的搜索、统计、热门标签查询等功能。
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    /**
     * 根据标签名称查找标签
     * @param tagName 标签名称
     * @return 标签实体（如果存在）
     */
    Optional<Tag> findByTagName(String tagName);
    
    /**
     * 检查标签名称是否存在
     * @param tagName 标签名称
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByTagName(String tagName);
    
    /**
     * 根据标签名称模糊搜索
     * @param tagName 标签名称关键字
     * @param pageable 分页参数
     * @return 匹配的标签列表
     */
    Page<Tag> findByTagNameContainingIgnoreCase(String tagName, Pageable pageable);
    
    /**
     * 获取所有标签并按名称排序
     * @return 按名称排序的标签列表
     */
    List<Tag> findAllByOrderByTagNameAsc();
    
    /**
     * 获取热门标签（按关联文章数量排序）
     * @param pageable 分页参数
     * @return 热门标签列表
     */
    @Query("SELECT t FROM Tag t LEFT JOIN t.articles a GROUP BY t ORDER BY COUNT(a) DESC")
    List<Tag> findPopularTags(Pageable pageable);
    
    /**
     * 获取最近创建的标签
     * @param pageable 分页参数
     * @return 最近创建的标签列表
     */
    List<Tag> findByOrderByCreatedTimeDesc(Pageable pageable);
    
    /**
     * 统计使用指定标签的文章数量
     * @param tagId 标签ID
     * @return 文章数量
     */
    @Query("SELECT COUNT(a) FROM Article a JOIN a.tags t WHERE t.tagId = :tagId")
    Long countArticlesByTagId(@Param("tagId") Long tagId);
    
    /**
     * 查找未被任何文章使用的标签
     * @return 未使用的标签列表
     */
    @Query("SELECT t FROM Tag t WHERE t.articles IS EMPTY")
    List<Tag> findUnusedTags();
    
    /**
     * 根据标签名称列表查找标签
     * @param tagNames 标签名称列表
     * @return 匹配的标签列表
     */
    List<Tag> findByTagNameIn(List<String> tagNames);
    
    /**
     * 获取标签使用统计（标签名称和使用次数）
     * @return 标签统计列表，每个元素包含[标签名称, 使用次数]
     */
    @Query("SELECT t.tagName, COUNT(a) FROM Tag t LEFT JOIN t.articles a GROUP BY t.tagId, t.tagName ORDER BY COUNT(a) DESC")
    List<Object[]> getTagUsageStatistics();
    
    /**
     * 搜索与指定文章相关的推荐标签
     * @param articleId 文章ID
     * @param limit 返回数量限制
     * @return 推荐标签列表
     */
    @Query(value = "SELECT DISTINCT t.* FROM tags t " +
                   "JOIN Article_Tags at1 ON t.tag_id = at1.tag_id " +
                   "JOIN Article_Tags at2 ON at1.article_id = at2.article_id " +
                   "WHERE at2.tag_id IN " +
                   "(SELECT at3.tag_id FROM Article_Tags at3 WHERE at3.article_id = :articleId) " +
                   "AND t.tag_id NOT IN " +
                   "(SELECT at4.tag_id FROM Article_Tags at4 WHERE at4.article_id = :articleId) " +
                   "GROUP BY t.tag_id ORDER BY COUNT(*) DESC LIMIT :limit", 
           nativeQuery = true)
    List<Tag> findRecommendedTagsForArticle(@Param("articleId") Long articleId, @Param("limit") int limit);
}
