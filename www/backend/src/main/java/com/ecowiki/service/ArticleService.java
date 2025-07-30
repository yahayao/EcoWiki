package com.ecowiki.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.dto.article.ArticleCreateRequest;
import com.ecowiki.dto.article.ArticleDto;
import com.ecowiki.dto.article.ArticleStatisticsDto;
import com.ecowiki.dto.article.ArticleUpdateRequest;
import com.ecowiki.entity.article.Article;
import com.ecowiki.entity.article.ArticleVersion;
import com.ecowiki.entity.tag.Tag;
import com.ecowiki.entity.user.User;
import com.ecowiki.repository.article.ArticleRepository;
import com.ecowiki.repository.user.UserRepository;

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
     * 文章版本服务接口
     */
    @Autowired
    private ArticleVersionService articleVersionService;
    
    /**
     * 标签服务接口
     */
    @Autowired
    private TagService tagService;
    
    /**
     * 用户数据访问接口
     */
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 用户服务接口
     */
    @Autowired
    private UserService userService;

    /**
     * 创建新文章
     * @param request 文章创建请求对象
     * @return 创建成功的文章DTO
     * @throws IllegalArgumentException 如果文章标题已存在
     */
    public ArticleDto createArticle(ArticleCreateRequest request) {
        // 检查标题是否已存在
        if (articleRepository.findByTitle(request.getTitle()).isPresent()) {
            throw new IllegalArgumentException("标题为 '" + request.getTitle() + "' 的文章已存在");
        }
        
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setAuthor(request.getAuthor());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        
        // 处理标签
        if (request.getTags() != null && !request.getTags().trim().isEmpty()) {
            Set<Tag> tags = tagService.parseAndCreateTags(request.getTags());
            article.setTags(tags);
        }
        
        article.setPublishDate(LocalDateTime.now());
        article.setViews(0);
        article.setLikes(0);
        article.setComments(0);
        
        Article savedArticle = articleRepository.save(article);
        
        // 为新文章创建初始版本
        try {
            articleVersionService.createVersion(savedArticle.getArticleId(), 
                savedArticle.getContent(), savedArticle.getAuthor());
        } catch (Exception e) {
            // 版本创建失败不影响文章创建，只记录日志
            System.err.println("Failed to create initial version for article " + 
                savedArticle.getArticleId() + ": " + e.getMessage());
        }
        
        return convertToDto(savedArticle);
    }

    /**
     * 检查文章标题是否已存在
     * @param title 文章标题
     * @return 如果标题已存在返回true，否则返回false
     */
    public boolean titleExists(String title) {
        return articleRepository.findByTitle(title).isPresent();
    }

    /**
     * 更新文章内容
     * @param articleId 文章ID
     * @param request 文章更新请求对象
     * @return 更新后的文章DTO
     * @throws RuntimeException 当文章不存在时抛出异常
     */
    public ArticleDto updateArticle(Long articleId, ArticleUpdateRequest request) {
        // 调用带编辑者参数的重载方法，使用原作者作为默认编辑者
        return updateArticle(articleId, request, null);
    }

    /**
     * 更新文章内容（带编辑者信息）
     * @param articleId 文章ID
     * @param request 文章更新请求对象
     * @param editorUsername 当前编辑者用户名（用于版本历史记录）
     * @return 更新后的文章DTO
     * @throws RuntimeException 当文章不存在时抛出异常
     */
    public ArticleDto updateArticle(Long articleId, ArticleUpdateRequest request, String editorUsername) {
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            throw new RuntimeException("文章不存在");
        }
        
        Article article = optionalArticle.get();
        
        // 保存旧内容用于比较
        String oldContent = article.getContent();
        String newContent = request.getContent();
        
        // 更新文章信息
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        
        // 更新标签
        article.clearTags(); // 清除现有标签关联
        if (request.getTags() != null && !request.getTags().trim().isEmpty()) {
            Set<Tag> tags = tagService.parseAndCreateTags(request.getTags());
            article.setTags(tags);
        }
        
        Article savedArticle = articleRepository.save(article);
        
        // 如果内容有变化，创建新版本
        if (!oldContent.equals(newContent)) {
            try {
                // 使用传入的编辑者用户名，如果为空则使用文章原作者
                String versionAuthor;
                if (editorUsername != null && !editorUsername.trim().isEmpty()) {
                    versionAuthor = editorUsername;
                } else {
                    versionAuthor = savedArticle.getAuthor() != null ? savedArticle.getAuthor() : "系统";
                }
                
                System.out.println("=== createVersion Debug ===");
                System.out.println("文章ID: " + articleId);
                System.out.println("传入的编辑者: " + editorUsername);
                System.out.println("实际版本作者: " + versionAuthor);
                System.out.println("原文章作者: " + savedArticle.getAuthor());
                
                articleVersionService.createVersion(articleId, newContent, versionAuthor);
            } catch (Exception e) {
                // 版本创建失败不影响文章更新，只记录日志
                System.err.println("Failed to create version for article " + articleId + ": " + e.getMessage());
            }
        }
        
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
        
        // 根据作者用户名获取用户头像
        Optional<User> userOpt = userRepository.findByUsername(article.getAuthor());
        String authorAvatar = userOpt.map(User::getAvatarUrl).orElse(null);
        dto.setAuthorAvatar(authorAvatar);
        
        dto.setContent(article.getContent());
        dto.setPublishDate(article.getPublishDate());
        dto.setCategory(article.getCategory());
        dto.setViews(article.getViews());
        dto.setLikes(article.getLikes());
        dto.setTags(article.getTagsAsString()); // 转换为逗号分隔的字符串
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

    /**
     * 获取文章贡献者列表
     * @param articleId 文章ID
     * @return 贡献者列表
     */
    public List<Map<String, Object>> getArticleContributors(Long articleId) {
        try {
            // 通过版本历史获取贡献者
            Page<ArticleVersion> versions = articleVersionService.getVersionHistory(articleId, 
                PageRequest.of(0, 100));
            
            // 获取文章信息以排除原作者
            Optional<Article> articleOpt = articleRepository.findById(articleId);
            String originalAuthor = articleOpt.map(Article::getAuthor).orElse("");
            
            // 统计每个编辑者的贡献
            Map<String, Map<String, Object>> contributorMap = new HashMap<>();
            
            for (ArticleVersion version : versions.getContent()) {
                String author = version.getAuthor();
                
                // 排除原作者
                if (author == null || author.equals(originalAuthor)) {
                    continue;
                }
                
                contributorMap.computeIfAbsent(author, k -> {
                    Map<String, Object> contributor = new HashMap<>();
                    contributor.put("username", k);
                    contributor.put("displayName", k);
                    
                    // 尝试获取用户头像URL
                    String avatarUrl = "";
                    try {
                        Optional<User> userOpt = userService.findByUsername(k);
                        if (userOpt.isPresent()) {
                            User user = userOpt.get();
                            avatarUrl = user.getAvatarUrl() != null ? user.getAvatarUrl() : "";
                        }
                    } catch (Exception e) {
                        System.err.println("获取用户头像失败: " + k + " - " + e.getMessage());
                    }
                    
                    contributor.put("avatarUrl", avatarUrl);
                    contributor.put("editCount", 0);
                    contributor.put("latestEdit", version.getCreatedAt());
                    return contributor;
                });
                
                Map<String, Object> contributor = contributorMap.get(author);
                contributor.put("editCount", (Integer) contributor.get("editCount") + 1);
                
                // 更新最新编辑时间
                LocalDateTime currentLatest = (LocalDateTime) contributor.get("latestEdit");
                if (version.getCreatedAt().isAfter(currentLatest)) {
                    contributor.put("latestEdit", version.getCreatedAt());
                }
            }
            
            // 转换为列表并排序
            return contributorMap.values().stream()
                .sorted((a, b) -> {
                    int editCountA = (Integer) a.get("editCount");
                    int editCountB = (Integer) b.get("editCount");
                    if (editCountA != editCountB) {
                        return editCountB - editCountA; // 编辑次数多的在前
                    }
                    LocalDateTime timeA = (LocalDateTime) a.get("latestEdit");
                    LocalDateTime timeB = (LocalDateTime) b.get("latestEdit");
                    return timeB.compareTo(timeA); // 时间新的在前
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("获取贡献者失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
