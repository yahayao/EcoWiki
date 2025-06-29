package com.ecowiki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    // 根据标题查找文章
    Optional<Article> findByTitle(String title);
    
    // 根据作者查找文章
    List<Article> findByAuthor(String author);
    
    // 根据分类查找文章
    List<Article> findByCategory(String category);
    Page<Article> findByCategory(String category, Pageable pageable);
    
    // 根据标题包含关键字查找文章
    List<Article> findByTitleContaining(String keyword);
    Page<Article> findByTitleContaining(String keyword, Pageable pageable);
    
    // 根据内容包含关键字查找文章
    List<Article> findByContentContaining(String keyword);
    Page<Article> findByContentContaining(String keyword, Pageable pageable);
    
    // 根据标题或内容包含关键字查找文章
    @Query("SELECT a FROM Article a WHERE a.title LIKE CONCAT('%', :keyword, '%') OR a.content LIKE CONCAT('%', :keyword, '%')")
    List<Article> findByTitleOrContentContaining(@Param("keyword") String keyword);
    
    @Query("SELECT a FROM Article a WHERE a.title LIKE CONCAT('%', :keyword, '%') OR a.content LIKE CONCAT('%', :keyword, '%')")
    Page<Article> findByTitleOrContentContaining(@Param("keyword") String keyword, Pageable pageable);
    
    // 根据标签查找文章
    @Query("SELECT a FROM Article a WHERE a.tags LIKE CONCAT('%', :tag, '%')")
    List<Article> findByTagsContaining(@Param("tag") String tag);
    
    @Query("SELECT a FROM Article a WHERE a.tags LIKE CONCAT('%', :tag, '%')")
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
    
    // 增加浏览量
    @Modifying
    @Query("UPDATE Article a SET a.views = a.views + 1 WHERE a.articleId = :articleId")
    void incrementViews(@Param("articleId") Long articleId);
    
    // 增加点赞数
    @Modifying
    @Query("UPDATE Article a SET a.likes = a.likes + 1 WHERE a.articleId = :articleId")
    void incrementLikes(@Param("articleId") Long articleId);
    
    // 减少点赞数
    @Modifying
    @Query("UPDATE Article a SET a.likes = CASE WHEN a.likes > 0 THEN a.likes - 1 ELSE 0 END WHERE a.articleId = :articleId")
    void decrementLikes(@Param("articleId") Long articleId);
    
    // 更新评论数
    @Modifying
    @Query("UPDATE Article a SET a.comments = :comments WHERE a.articleId = :articleId")
    void updateComments(@Param("articleId") Long articleId, @Param("comments") Integer comments);
    
    // 获取分类统计
    @Query("SELECT a.category, COUNT(a) FROM Article a GROUP BY a.category")
    List<Object[]> getCategoryStats();
    
    // 获取作者统计
    @Query("SELECT a.author, COUNT(a) FROM Article a GROUP BY a.author")
    List<Object[]> getAuthorStats();
    
    // 获取总浏览量
    @Query("SELECT SUM(a.views) FROM Article a")
    Long getTotalViews();
    
    // 获取总点赞数
    @Query("SELECT SUM(a.likes) FROM Article a")
    Long getTotalLikes();
}
