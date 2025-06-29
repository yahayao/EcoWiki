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

@Service
@Transactional
public class ArticleService {
    
    @Autowired
    private ArticleRepository articleRepository;
    
    // 创建文章
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
    
    // 更新文章
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
    
    // 根据ID获取文章
    public ArticleDto getArticleById(Long articleId) {
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            throw new RuntimeException("文章不存在");
        }
        return convertToDto(optionalArticle.get());
    }
    
    // 根据ID获取文章并增加浏览量
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
    
    // 删除文章
    public void deleteArticle(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new RuntimeException("文章不存在");
        }
        articleRepository.deleteById(articleId);
    }
    
    // 获取所有文章（分页）
    public Page<ArticleDto> getAllArticles(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Article> articles = articleRepository.findAll(pageable);
        return articles.map(this::convertToDto);
    }
    
    // 根据分类获取文章（分页）
    public Page<ArticleDto> getArticlesByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishDate").descending());
        Page<Article> articles = articleRepository.findByCategory(category, pageable);
        return articles.map(this::convertToDto);
    }
    
    // 根据作者获取文章
    public List<ArticleDto> getArticlesByAuthor(String author) {
        List<Article> articles = articleRepository.findByAuthor(author);
        return articles.stream().map(this::convertToDto).toList();
    }
    
    // 搜索文章（根据标题或内容）
    public Page<ArticleDto> searchArticles(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishDate").descending());
        Page<Article> articles = articleRepository.findByTitleOrContentContaining(keyword, pageable);
        return articles.map(this::convertToDto);
    }
    
    // 根据标签获取文章
    public Page<ArticleDto> getArticlesByTag(String tag, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishDate").descending());
        Page<Article> articles = articleRepository.findByTagsContaining(tag, pageable);
        return articles.map(this::convertToDto);
    }
    
    // 获取热门文章
    public List<ArticleDto> getPopularArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Article> articles = articleRepository.findPopularArticles(pageable);
        return articles.stream().map(this::convertToDto).toList();
    }
    
    // 获取最新文章
    public List<ArticleDto> getLatestArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Article> articles = articleRepository.findLatestArticles(pageable);
        return articles.stream().map(this::convertToDto).toList();
    }
    
    // 点赞文章
    public void likeArticle(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new RuntimeException("文章不存在");
        }
        articleRepository.incrementLikes(articleId);
    }
    
    // 取消点赞
    public void unlikeArticle(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new RuntimeException("文章不存在");
        }
        articleRepository.decrementLikes(articleId);
    }
    
    // 更新评论数
    public void updateCommentsCount(Long articleId, Integer commentsCount) {
        if (!articleRepository.existsById(articleId)) {
            throw new RuntimeException("文章不存在");
        }
        articleRepository.updateComments(articleId, commentsCount);
    }
    
    // 获取统计信息
    public ArticleStatisticsDto getStatistics() {
        long totalArticles = articleRepository.count();
        Long totalViews = articleRepository.getTotalViews();
        Long totalLikes = articleRepository.getTotalLikes();
        
        return new ArticleStatisticsDto(totalArticles, totalViews, totalLikes);
    }
    
    // 将Entity转换为DTO
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
}
