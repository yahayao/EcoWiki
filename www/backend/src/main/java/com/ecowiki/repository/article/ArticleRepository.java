/**
 * 文章数据访问层接口
 * 
 * 功能：
 * - 提供文章实体的数据库操作接口
 * - 支持文章的CRUD操作和复杂查询
 * - 实现文章搜索、分类筛选和统计功能
 * - 提供分页查询和排序功能
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.repository.article;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.article.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    /**
     * 根据标题查找文章
     * @param title 文章标题
     * @return 文章实体（可能为空）
     */
    Optional<Article> findByTitle(String title);
    
    /**
     * 根据作者查找文章
     * @param author 作者名
     * @return 文章列表
     */
    List<Article> findByAuthor(String author);
    
    /**
     * 根据分类查找文章
     * @param category 分类名
     * @return 文章列表
     */
    List<Article> findByCategory(String category);
    
    /**
     * 根据分类分页查找文章
     * @param category 分类名
     * @param pageable 分页参数
     * @return 文章分页数据
     */
    Page<Article> findByCategory(String category, Pageable pageable);
    
    /**
     * 根据标题包含关键字查找文章
     * @param keyword 关键字
     * @return 文章列表
     */
    List<Article> findByTitleContaining(String keyword);
    
    /**
     * 根据标题包含关键字分页查找文章
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 文章分页数据
     */
    Page<Article> findByTitleContaining(String keyword, Pageable pageable);
    
    /**
     * 根据内容包含关键字查找文章
     * @param keyword 关键字
     * @return 文章列表
     */
    List<Article> findByContentContaining(String keyword);
    
    /**
     * 根据内容包含关键字分页查找文章
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 文章分页数据
     */
    Page<Article> findByContentContaining(String keyword, Pageable pageable);
    
    /**
     * 根据标题或内容包含关键字查找文章
     * @param keyword 关键字
     * @return 文章列表
     */
    @Query("SELECT a FROM Article a WHERE a.title LIKE CONCAT('%', :keyword, '%') OR a.content LIKE CONCAT('%', :keyword, '%')")
    List<Article> findByTitleOrContentContaining(@Param("keyword") String keyword);
    
    /**
     * 根据标题或内容包含关键字分页查找文章
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 文章分页数据
     */
    @Query("SELECT a FROM Article a WHERE a.title LIKE CONCAT('%', :keyword, '%') OR a.content LIKE CONCAT('%', :keyword, '%')")
    Page<Article> findByTitleOrContentContaining(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据标签查找文章
     * @param tag 标签名
     * @return 文章列表
     */
    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.tagName = :tag")
    List<Article> findByTagsContaining(@Param("tag") String tag);
    
    /**
     * 根据标签分页查找文章
     * @param tag 标签名
     * @param pageable 分页参数
     * @return 文章分页数据
     */
    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.tagName = :tag")
    Page<Article> findByTagsContaining(@Param("tag") String tag, Pageable pageable);
    
    // 按发布时间倒序查找文章
    List<Article> findAllByOrderByPublishDateDesc();
    Page<Article> findAllByOrderByPublishDateDesc(Pageable pageable);
    
    // 按浏览量倒序查找文章
    List<Article> findAllByOrderByViewsDesc();
    Page<Article> findAllByOrderByViewsDesc(Pageable pageable);
    
    // 按点赞数倒序查找文章
    List<Article> findAllByOrderByLikesDesc();
    Page<Article> findAllByOrderByLikesDesc(Pageable pageable);
    
    // 获取热门文章（根据浏览量或点赞数）
    @Query("SELECT a FROM Article a ORDER BY (a.views + a.likes * 2) DESC")
    List<Article> findPopularArticles(Pageable pageable);
    
    // 获取最新文章
    @Query("SELECT a FROM Article a ORDER BY a.publishDate DESC")
    List<Article> findLatestArticles(Pageable pageable);
    
    /**
     * 增加文章浏览量
     * @param articleId 文章ID
     */
    @Modifying
    @Query("UPDATE Article a SET a.views = a.views + 1 WHERE a.articleId = :articleId")
    void incrementViews(@Param("articleId") Long articleId);
    
    /**
     * 增加文章点赞数
     * @param articleId 文章ID
     */
    @Modifying
    @Query("UPDATE Article a SET a.likes = a.likes + 1 WHERE a.articleId = :articleId")
    void incrementLikes(@Param("articleId") Long articleId);
    
    /**
     * 减少文章点赞数
     * @param articleId 文章ID
     */
    @Modifying
    @Query("UPDATE Article a SET a.likes = CASE WHEN a.likes > 0 THEN a.likes - 1 ELSE 0 END WHERE a.articleId = :articleId")
    void decrementLikes(@Param("articleId") Long articleId);
    
    /**
     * 更新文章评论数
     * @param articleId 文章ID
     * @param comments 评论数
     */
    @Modifying
    @Query("UPDATE Article a SET a.comments = :comments WHERE a.articleId = :articleId")
    void updateComments(@Param("articleId") Long articleId, @Param("comments") Integer comments);
    
    /**
     * 获取文章总浏览量
     * @return 总浏览量
     */
    @Query("SELECT SUM(a.views) FROM Article a")
    Long getTotalViews();
    
    /**
     * 获取文章总点赞数
     * @return 总点赞数
     */
    @Query("SELECT SUM(a.likes) FROM Article a")
    Long getTotalLikes();
        
    /**
     * 获取所有文章ID列表
     * @return 文章ID列表
     */
    @Query("SELECT a.articleId FROM Article a")
    List<Long> findAllArticleIds();
    
    /**
     * 获取指定文章的评论数
     * @param articleId 文章ID
     * @return 评论数
     */
    @Query("SELECT a.comments FROM Article a WHERE a.articleId = :articleId")
    Integer getCommentsCount(@Param("articleId") Long articleId);
    
    /**
     * 根据作者统计文章数量
     * @param author 作者用户名
     * @return 文章数量
     */
    long countByAuthor(String author);
    
    /**
     * 根据作者查找文章
     * @param author 作者用户名
     * @param pageable 分页参数
     * @return 文章分页结果
     */
    Page<Article> findByAuthor(String author, Pageable pageable);
}
