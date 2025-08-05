package com.ecowiki.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.dto.article.ArticleCreateRequest;
import com.ecowiki.dto.article.ArticleUpdateRequest;
import com.ecowiki.dto.article.ReviewDraftRequest;
import com.ecowiki.entity.article.Article;
import com.ecowiki.entity.article.ArticleDraft;
import com.ecowiki.entity.user.User;
import com.ecowiki.repository.article.ArticleDraftRepository;
import com.ecowiki.repository.article.ArticleRepository;
import com.ecowiki.repository.user.UserRepository;

/**
 * 文章草稿服务类
 * <p>
 * 提供文章草稿相关的业务逻辑处理，包括草稿的创建、审核、发布等功能。
 * 实现完整的文章审核流程，确保内容质量控制。
 * <p>
 * <b>审核流程：</b>
 * 1. 用户提交文章编辑 -> 创建草稿（状态：PENDING）
 * 2. 自动发送审核通知给superadmin
 * 3. superadmin审核 -> 通过（APPROVED）或拒绝（REJECTED）
 * 4. 通过：发布到articles表；拒绝：发送拒绝通知给用户
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2025-07-31
 */
@Service
@Transactional
public class ArticleDraftService {
    
    @Autowired
    private ArticleDraftRepository articleDraftRepository;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private ArticleService articleService;

    /**
     * 提交新文章草稿（用户创建新文章）
     * @param request 文章创建请求
     * @param editorUserId 编辑者用户ID
     * @return 创建的草稿对象
     */
    public ArticleDraft submitNewArticleDraft(ArticleCreateRequest request, Long editorUserId) {
        // 检查标题是否已存在
        if (articleRepository.findByTitle(request.getTitle()).isPresent()) {
            throw new IllegalArgumentException("标题为 '" + request.getTitle() + "' 的文章已存在");
        }
        
        // 为新文章先创建一个草稿状态的 Article 记录
        Article draftArticle = new Article();
        draftArticle.setTitle(request.getTitle());
        draftArticle.setAuthor(request.getAuthor());
        draftArticle.setContent(request.getContent());
        draftArticle.setCategory(request.getCategory());
        draftArticle.setPublishDate(LocalDateTime.now());
        draftArticle.setViews(0);
        draftArticle.setLikes(0);
        draftArticle.setComments(0);
        
        // 保存草稿文章，获取分配的 ID
        Article savedArticle = articleRepository.save(draftArticle);
        
        // 创建对应的草稿记录
        ArticleDraft draft = new ArticleDraft();
        draft.setArticleId(savedArticle.getArticleId()); // 关联刚创建的文章ID
        draft.setEditorUserId(editorUserId);
        draft.setTitle(request.getTitle());
        draft.setContent(request.getContent());
        draft.setCategory(request.getCategory());
        draft.setTags(request.getTags());
        draft.setReviewStatus(ArticleDraft.ReviewStatus.PENDING);
        draft.setSubmittedAt(LocalDateTime.now());
        
        ArticleDraft savedDraft = articleDraftRepository.save(draft);
        
        // 发送审核通知给superadmin
        sendReviewNotificationToSuperAdmin(savedDraft, "新文章待审核");
        
        return savedDraft;
    }

    /**
     * 提交文章编辑草稿（用户编辑现有文章）
     * @param articleId 文章ID
     * @param request 文章更新请求
     * @param editorUserId 编辑者用户ID
     * @return 创建的草稿对象
     */
    public ArticleDraft submitArticleEditDraft(Long articleId, ArticleUpdateRequest request, Long editorUserId) {
        // 验证文章是否存在
        Optional<Article> articleOpt = articleRepository.findById(articleId);
        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("文章不存在，ID: " + articleId);
        }
        
        ArticleDraft draft = new ArticleDraft();
        draft.setArticleId(articleId); // 编辑现有文章
        draft.setEditorUserId(editorUserId);
        draft.setTitle(request.getTitle());
        draft.setContent(request.getContent());
        draft.setCategory(request.getCategory());
        draft.setTags(request.getTags());
        draft.setReviewStatus(ArticleDraft.ReviewStatus.PENDING);
        draft.setSubmittedAt(LocalDateTime.now());
        
        ArticleDraft savedDraft = articleDraftRepository.save(draft);
        
        // 发送审核通知给superadmin
        sendReviewNotificationToSuperAdmin(savedDraft, "文章编辑待审核");
        
        return savedDraft;
    }

    /**
     * 审核草稿
     * @param draftId 草稿ID
     * @param request 审核请求
     * @param reviewerUserId 审核者用户ID
     * @return 审核后的草稿对象
     */
    public ArticleDraft reviewDraft(Long draftId, ReviewDraftRequest request, Long reviewerUserId) {
        Optional<ArticleDraft> draftOpt = articleDraftRepository.findById(draftId);
        if (draftOpt.isEmpty()) {
            throw new IllegalArgumentException("草稿不存在，ID: " + draftId);
        }
        
        ArticleDraft draft = draftOpt.get();
        
        // 检查是否已经审核过
        if (draft.getReviewStatus() != ArticleDraft.ReviewStatus.PENDING) {
            throw new IllegalStateException("该草稿已经审核过，当前状态: " + draft.getReviewStatus().getDescription());
        }
        
        // 更新审核信息
        draft.setReviewStatus(request.getApproved() ? 
            ArticleDraft.ReviewStatus.APPROVED : ArticleDraft.ReviewStatus.REJECTED);
        draft.setReviewedAt(LocalDateTime.now());
        draft.setReviewNotes(request.getReviewNotes());
        
        ArticleDraft savedDraft = articleDraftRepository.save(draft);
        
        if (request.getApproved()) {
            // 审核通过，发布文章
            publishApprovedDraft(savedDraft);
            // 发送通过通知给编辑者
            sendApprovalNotificationToEditor(savedDraft, reviewerUserId);
        } else {
            // 审核拒绝，保留 article 记录和草稿记录作为历史记录
            // 发送拒绝通知给编辑者
            sendRejectionNotificationToEditor(savedDraft, reviewerUserId);
        }
        
        return savedDraft;
    }

    /**
     * 发布已通过审核的草稿
     * @param draft 已通过审核的草稿
     */
    private void publishApprovedDraft(ArticleDraft draft) {
        // 现在所有草稿都应该有 articleId，直接更新对应的文章记录
        if (draft.getArticleId() != null) {
            updateExistingArticleFromDraft(draft);
        } else {
            // 这种情况不应该发生，但保留兼容性
            createNewArticleFromDraft(draft);
        }
        
        // 发布成功后，草稿保留作为历史记录，状态已经是APPROVED
    }

    /**
     * 从草稿创建新文章
     * @param draft 草稿对象
     */
    private void createNewArticleFromDraft(ArticleDraft draft) {
        // 获取编辑者用户信息
        Optional<User> editorOpt = userRepository.findById(draft.getEditorUserId());
        String authorName = editorOpt.map(User::getUsername).orElse("未知用户");
        
        ArticleCreateRequest createRequest = new ArticleCreateRequest();
        createRequest.setTitle(draft.getTitle());
        createRequest.setAuthor(authorName);
        createRequest.setContent(draft.getContent());
        createRequest.setCategory(draft.getCategory());
        createRequest.setTags(draft.getTags());
        
        // 使用现有的文章服务创建文章
        articleService.createArticle(createRequest);
    }

    /**
     * 从草稿更新现有文章
     * @param draft 草稿对象
     */
    private void updateExistingArticleFromDraft(ArticleDraft draft) {
        // 获取编辑者用户信息
        Optional<User> editorOpt = userRepository.findById(draft.getEditorUserId());
        String editorName = editorOpt.map(User::getUsername).orElse("未知用户");
        
        ArticleUpdateRequest updateRequest = new ArticleUpdateRequest();
        updateRequest.setTitle(draft.getTitle());
        updateRequest.setContent(draft.getContent());
        updateRequest.setCategory(draft.getCategory());
        updateRequest.setTags(draft.getTags());
        
        // 使用现有的文章服务更新文章
        articleService.updateArticle(draft.getArticleId(), updateRequest, editorName);
    }

    /**
     * 发送审核通知给superadmin
     * @param draft 草稿对象
     * @param notificationType 通知类型
     */
    private void sendReviewNotificationToSuperAdmin(ArticleDraft draft, String notificationType) {
        // 查找superadmin用户
        Optional<User> superAdminOpt = userRepository.findByUsername("superadmin");
        if (superAdminOpt.isEmpty()) {
            System.err.println("警告：未找到superadmin用户，无法发送审核通知");
            return;
        }
        
        // 获取编辑者用户信息
        Optional<User> editorOpt = userRepository.findById(draft.getEditorUserId());
        String editorName = editorOpt.map(User::getUsername).orElse("未知用户");
        
        User superAdmin = superAdminOpt.get();
        String messageContent = String.format(
                """
                        📝 %s
                        
                        文章标题：%s
                        编辑者：%s
                        提交时间：%s
                        文章分类：%s
                        
                        请及时前往管理后台进行审核。
                        草稿ID：%d""",
            notificationType,
            draft.getTitle(),
            editorName,
            draft.getSubmittedAt().toString(),
            draft.getCategory() != null ? draft.getCategory() : "未分类",
            draft.getDraftId()
        );
        
        try {
            messageService.sendMessage(draft.getEditorUserId().intValue(), superAdmin.getUserId().intValue(), messageContent);
        } catch (Exception e) {
            System.err.println("发送审核通知失败: " + e.getMessage());
        }
    }

    /**
     * 发送审核通过通知给编辑者
     * @param draft 草稿对象
     * @param reviewerUserId 审核者用户ID
     */
    private void sendApprovalNotificationToEditor(ArticleDraft draft, Long reviewerUserId) {
        // 获取审核者用户信息
        Optional<User> reviewerOpt = userRepository.findById(reviewerUserId);
        String reviewerName = reviewerOpt.map(User::getUsername).orElse("管理员");
        
        String messageContent = String.format(
                """
                        ✅ 文章审核通过
                        
                        您提交的文章《%s》已通过审核并成功发布！
                        
                        审核者：%s
                        审核时间：%s
                        %s
                        
                        感谢您为EcoWiki的贡献！""",
            draft.getTitle(),
            reviewerName,
            draft.getReviewedAt().toString(),
            draft.getReviewNotes() != null ? ("审核备注：" + draft.getReviewNotes()) : ""
        );
        
        try {
            messageService.sendMessage(reviewerUserId.intValue(), draft.getEditorUserId().intValue(), messageContent);
        } catch (Exception e) {
            System.err.println("发送审核通过通知失败: " + e.getMessage());
        }
    }

    /**
     * 发送审核拒绝通知给编辑者
     * @param draft 草稿对象
     * @param reviewerUserId 审核者用户ID
     */
    private void sendRejectionNotificationToEditor(ArticleDraft draft, Long reviewerUserId) {
        // 获取审核者用户信息
        Optional<User> reviewerOpt = userRepository.findById(reviewerUserId);
        String reviewerName = reviewerOpt.map(User::getUsername).orElse("管理员");
        
        String messageContent = String.format(
                """
                        ❌ 文章审核未通过
                        
                        很抱歉，您提交的文章《%s》未能通过审核。
                        
                        审核者：%s
                        审核时间：%s
                        %s
                        
                        您可以根据反馈意见修改后重新提交。草稿已保留在系统中。""",
            draft.getTitle(),
            reviewerName,
            draft.getReviewedAt().toString(),
            draft.getReviewNotes() != null ? ("拒绝原因：" + draft.getReviewNotes()) : "未提供拒绝原因"
        );
        
        try {
            messageService.sendMessage(reviewerUserId.intValue(), draft.getEditorUserId().intValue(), messageContent);
        } catch (Exception e) {
            System.err.println("发送审核拒绝通知失败: " + e.getMessage());
        }
    }

    /**
     * 获取待审核草稿列表（分页）
     * @param pageable 分页参数
     * @return 待审核草稿列表
     */
    public Page<ArticleDraft> getPendingDrafts(Pageable pageable) {
        return articleDraftRepository.findPendingDrafts(pageable);
    }

    /**
     * 获取用户的草稿列表（分页）
     * @param editorUserId 编辑者用户ID
     * @param pageable 分页参数
     * @return 用户的草稿列表
     */
    public Page<ArticleDraft> getUserDrafts(Long editorUserId, Pageable pageable) {
        return articleDraftRepository.findByEditorUserId(editorUserId, pageable);
    }

    /**
     * 根据ID获取草稿详情
     * @param draftId 草稿ID
     * @return 草稿对象
     */
    public Optional<ArticleDraft> getDraftById(Long draftId) {
        return articleDraftRepository.findById(draftId);
    }

    /**
     * 获取所有草稿列表（分页）
     * @param pageable 分页参数
     * @return 所有草稿列表
     */
    public Page<ArticleDraft> getAllDrafts(Pageable pageable) {
        return articleDraftRepository.findAll(pageable);
    }

    /**
     * 根据审核状态获取草稿列表（分页）
     * @param reviewStatus 审核状态
     * @param pageable 分页参数
     * @return 指定状态的草稿列表
     */
    public Page<ArticleDraft> getDraftsByStatus(ArticleDraft.ReviewStatus reviewStatus, Pageable pageable) {
        return articleDraftRepository.findByReviewStatus(reviewStatus, pageable);
    }

    /**
     * 删除草稿
     * @param draftId 草稿ID
     * @param operatorUserId 操作者用户ID
     */
    public void deleteDraft(Long draftId, Long operatorUserId) {
        Optional<ArticleDraft> draftOpt = articleDraftRepository.findById(draftId);
        if (draftOpt.isEmpty()) {
            throw new IllegalArgumentException("草稿不存在，ID: " + draftId);
        }
        
        ArticleDraft draft = draftOpt.get();
        
        // 只有草稿作者或管理员可以删除草稿
        // 这里简化处理，实际应该检查权限
        if (!draft.getEditorUserId().equals(operatorUserId)) {
            // 检查操作者是否为管理员
            Optional<User> operatorOpt = userRepository.findById(operatorUserId);
            if (operatorOpt.isEmpty() || !"admin".equals(operatorOpt.get().getUserGroup())) {
                throw new IllegalArgumentException("无权限删除此草稿");
            }
        }
        
        articleDraftRepository.deleteById(draftId);
    }
}
