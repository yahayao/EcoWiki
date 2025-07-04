package com.ecowiki.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.dto.ArticleCreateRequest;
import com.ecowiki.dto.ArticleDto;
import com.ecowiki.dto.ArticleStatisticsDto;
import com.ecowiki.dto.ArticleUpdateRequest;
import com.ecowiki.entity.Article;
import com.ecowiki.repository.ArticleRepository;

/**
 * 文章服务类
 * <p>
 * 提供文章相关的业务逻辑处理，包括文章的增删改查、搜索、分类、标签、统计、互动等功能。
 * 依赖ArticleRepository进行数据访问，负责Entity与DTO之间的转换。
 * <p>
 * <b>设计说明：</b>
 * - 采用事务管理确保数据一致性
 * - 支持分页、排序、条件查询等功能
 * - 提供丰富的搜索和推荐功能
 * - 适用于Wiki内容管理、文章展示、社区互动等场景
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
@Service
@Transactional
public class ArticleService {
    /**
     * 文章数据访问接口
     */
    @Autowired
    private ArticleRepository articleRepository;

    /**
     * 创建新文章
     * @param request 文章创建请求对象
     * @return 创建成功的文章DTO
     */
    public ArticleDto createArticle(ArticleCreateRequest request) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setAuthor(request.getAuthor());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        article.setTags(request.getTags());
        article.setPublishDate(LocalDateTime.now());
        article.setViews(0);
        article.setLikes(0);
        article.setComments(0);
        
        Article savedArticle = articleRepository.save(article);
        return convertToDto(savedArticle);
    }

    /**
     * 更新文章内容
     * @param articleId 文章ID
     * @param request 文章更新请求对象
     * @return 更新后的文章DTO
     * @throws RuntimeException 当文章不存在时抛出异常
     */
    public ArticleDto updateArticle(Long articleId, ArticleUpdateRequest request) {
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            throw new RuntimeException("文章不存在");
        }
        
        Article article = optionalArticle.get();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        article.setTags(request.getTags());
        
        Article savedArticle = articleRepository.save(article);
        return convertToDto(savedArticle);
    }

    /**
     * 根据ID获取文章
     * @param articleId 文章ID
     * @return 文章DTO
     * @throws RuntimeException 当文章不存在时抛出异常
     */
    public ArticleDto getArticleById(Long articleId) {
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            throw new RuntimeException("文章不存在");
        }
        return convertToDto(optionalArticle.get());
    }

    /**
     * 根据ID获取文章并自动增加浏览量
     * @param articleId 文章ID
     * @return 文章DTO（包含最新浏览量）
     * @throws RuntimeException 当文章不存在时抛出异常
     */
    public ArticleDto getArticleByIdAndIncrementViews(Long articleId) {
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            throw new RuntimeException("文章不存在");
        }
        
        // 增加浏览量
        articleRepository.incrementViews(articleId);
        
        // 重新获取更新后的文章
        Article article = articleRepository.findById(articleId).get();
        return convertToDto(article);
    }

    /**
     * 删除文章
     * @param articleId 文章ID
     * @throws RuntimeException 当文章不存在时抛出异常
     */
    public void deleteArticle(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new RuntimeException("文章不存在");
        }
        articleRepository.deleteById(articleId);
    }

    /**
     * 分页获取所有文章
     * @param page 页码
     * @param size 每页数量
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @return 文章分页数据
     */
    public Page<ArticleDto> getAllArticles(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Article> articles = articleRepository.findAll(pageable);
        return articles.map(this::convertToDto);
    }

    /**
     * 根据分类分页获取文章
     * @param category 分类名
     * @param page 页码
     * @param size 每页数量
     * @return 文章分页数据
     */
    public Page<ArticleDto> getArticlesByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishDate").descending());
        Page<Article> articles = articleRepository.findByCategory(category, pageable);
        return articles.map(this::convertToDto);
    }

    /**
     * 根据作者获取文章列表
     * @param author 作者名
     * @return 文章DTO列表
     */
    public List<ArticleDto> getArticlesByAuthor(String author) {
        List<Article> articles = articleRepository.findByAuthor(author);
        return articles.stream().map(this::convertToDto).toList();
    }

    /**
     * 搜索文章（根据标题或内容包含关键字）
     * @param keyword 搜索关键字
     * @param page 页码
     * @param size 每页数量
     * @return 搜索结果分页数据
     */
    public Page<ArticleDto> searchArticles(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishDate").descending());
        Page<Article> articles = articleRepository.findByTitleOrContentContaining(keyword, pageable);
        return articles.map(this::convertToDto);
    }

    /**
     * 根据标签分页获取文章
     * @param tag 标签名
     * @param page 页码
     * @param size 每页数量
     * @return 文章分页数据
     */
    public Page<ArticleDto> getArticlesByTag(String tag, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishDate").descending());
        Page<Article> articles = articleRepository.findByTagsContaining(tag, pageable);
        return articles.map(this::convertToDto);
    }

    /**
     * 获取热门文章（基于浏览量和点赞数）
     * @param limit 返回数量限制
     * @return 热门文章DTO列表
     */
    public List<ArticleDto> getPopularArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Article> articles = articleRepository.findPopularArticles(pageable);
        return articles.stream().map(this::convertToDto).toList();
    }

    /**
     * 获取最新文章（按发布时间排序）
     * @param limit 返回数量限制
     * @return 最新文章DTO列表
     */
    public List<ArticleDto> getLatestArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Article> articles = articleRepository.findLatestArticles(pageable);
        return articles.stream().map(this::convertToDto).toList();
    }

    /**
     * 点赞文章
     * @param articleId 文章ID
     * @throws RuntimeException 当文章不存在时抛出异常
     */
    public void likeArticle(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new RuntimeException("文章不存在");
        }
        articleRepository.incrementLikes(articleId);
    }

    /**
     * 取消点赞文章
     * @param articleId 文章ID
     * @throws RuntimeException 当文章不存在时抛出异常
     */
    public void unlikeArticle(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new RuntimeException("文章不存在");
        }
        articleRepository.decrementLikes(articleId);
    }

    /**
     * 更新文章评论数
     * @param articleId 文章ID
     * @param commentsCount 新的评论数
     * @throws RuntimeException 当文章不存在时抛出异常
     */
    public void updateCommentsCount(Long articleId, Integer commentsCount) {
        if (!articleRepository.existsById(articleId)) {
            throw new RuntimeException("文章不存在");
        }
        articleRepository.updateComments(articleId, commentsCount);
    }

    /**
     * 获取文章统计信息
     * @return 文章统计DTO（总数、总浏览量、总点赞数等）
     */
    public ArticleStatisticsDto getStatistics() {
        long totalArticles = articleRepository.count();
        Long totalViews = articleRepository.getTotalViews();
        Long totalLikes = articleRepository.getTotalLikes();
        
        return new ArticleStatisticsDto(totalArticles, totalViews, totalLikes);
    }

    /**
     * 将文章实体转换为DTO
     * @param article 文章实体
     * @return 文章DTO
     */
    private ArticleDto convertToDto(Article article) {
        ArticleDto dto = new ArticleDto();
        dto.setArticleId(article.getArticleId());
        dto.setTitle(article.getTitle());
        dto.setAuthor(article.getAuthor());
        dto.setContent(article.getContent());
        dto.setPublishDate(article.getPublishDate());
        dto.setCategory(article.getCategory());
        dto.setViews(article.getViews());
        dto.setLikes(article.getLikes());
        dto.setTags(article.getTags());
        dto.setComments(article.getComments());
        dto.setUpdateTime(article.getUpdateTime());
        return dto;
    }

    /**
     * 根据标题获取文章ID
     * @param title 文章标题
     * @return 文章ID
     */
    public Long getArticleIdByTitle(String title) {
        Optional<Article> articleOpt = articleRepository.findByTitle(title);
        return articleOpt.map(Article::getArticleId).orElse(null);
    }
}
