package com.ecowiki.controller.article;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.dto.article.ArticleCreateRequest;
import com.ecowiki.dto.article.ArticleUpdateRequest;
import com.ecowiki.dto.article.ReviewDraftRequest;
import com.ecowiki.entity.article.ArticleDraft;
import com.ecowiki.entity.user.User;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.ArticleDraftService;
import com.ecowiki.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 文章草稿控制器
 * <p>
 * 提供文章草稿相关的RESTful API，包括草稿的提交、审核、查询等功能。
 * 实现完整的文章审核流程管理。
 * <p>
 * <b>主要功能：</b>
 * - 用户提交文章草稿（新建/编辑）
 * - 管理员审核草稿（通过/拒绝）
 * - 草稿列表查询（分页、按状态筛选）
 * - 草稿详情查看
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2025-07-31
 */
@RestController
@RequestMapping("/api/drafts")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@Validated
public class ArticleDraftController {
    
    @Autowired
    private ArticleDraftService articleDraftService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前请求用户
     */
    private User getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            try {
                String username = jwtUtil.extractUsername(token);
                Optional<User> userOpt = userService.findByUsername(username);
                return userOpt.orElse(null);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 检查用户是否为管理员
     */
    private boolean isUserAdmin(User user) {
        if (user == null) return true;
        List<String> roles = userService.getUserRoleNames(user.getUserId().intValue());
        return !roles.contains("admin") && !roles.contains("superadmin");
    }

    /**
     * 从请求中提取JWT token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 提交新文章草稿
     * @param request 文章创建请求
     * @param httpRequest HTTP请求
     * @return 创建的草稿信息
     */
    @PostMapping("/submit-new")
    public ResponseEntity<ApiResponse<ArticleDraft>> submitNewArticleDraft(
            @RequestBody @Validated ArticleCreateRequest request,
            HttpServletRequest httpRequest) {
        
        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "请先登录"));
        }
        
        try {
            ArticleDraft draft = articleDraftService.submitNewArticleDraft(request, currentUser.getUserId());
            return ResponseEntity.ok(ApiResponse.success(draft, "文章草稿提交成功，等待审核"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "提交草稿失败: " + e.getMessage()));
        }
    }

    /**
     * 提交文章编辑草稿
     * @param articleId 文章ID
     * @param request 文章更新请求
     * @param httpRequest HTTP请求
     * @return 创建的草稿信息
     */
    @PostMapping("/submit-edit/{articleId}")
    public ResponseEntity<ApiResponse<ArticleDraft>> submitArticleEditDraft(
            @PathVariable Long articleId,
            @RequestBody @Validated ArticleUpdateRequest request,
            HttpServletRequest httpRequest) {
        
        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "请先登录"));
        }
        
        try {
            ArticleDraft draft = articleDraftService.submitArticleEditDraft(articleId, request, currentUser.getUserId());
            return ResponseEntity.ok(ApiResponse.success(draft, "文章编辑草稿提交成功，等待审核"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "提交草稿失败: " + e.getMessage()));
        }
    }

    /**
     * 审核草稿（仅管理员）
     * @param draftId 草稿ID
     * @param request 审核请求
     * @param httpRequest HTTP请求
     * @return 审核结果
     */
    @PutMapping("/review/{draftId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<ArticleDraft>> reviewDraft(
            @PathVariable Long draftId,
            @RequestBody @Validated ReviewDraftRequest request,
            HttpServletRequest httpRequest) {
        
        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "请先登录"));
        }
        
        // 检查是否为管理员
        if (isUserAdmin(currentUser)) {
            return ResponseEntity.ok(ApiResponse.error(403, "只有管理员可以审核草稿"));
        }
        
        try {
            ArticleDraft reviewedDraft = articleDraftService.reviewDraft(draftId, request, currentUser.getUserId());
            String message = request.getApproved() ? "草稿审核通过，文章已发布" : "草稿审核未通过";
            return ResponseEntity.ok(ApiResponse.success(reviewedDraft, message));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "审核失败: " + e.getMessage()));
        }
    }

    /**
     * 获取待审核草稿列表（仅管理员）
     * @param page 页码
     * @param size 每页大小
     * @param httpRequest HTTP请求
     * @return 待审核草稿列表
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<Page<ArticleDraft>>> getPendingDrafts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        
        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "请先登录"));
        }
        
        // 检查是否为管理员
        if (isUserAdmin(currentUser)) {
            return ResponseEntity.ok(ApiResponse.error(403, "只有管理员可以查看待审核草稿"));
        }
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
            Page<ArticleDraft> drafts = articleDraftService.getPendingDrafts(pageable);
            return ResponseEntity.ok(ApiResponse.success(drafts, "获取待审核草稿列表成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "获取草稿列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户的草稿列表
     * @param page 页码
     * @param size 每页大小
     * @param httpRequest HTTP请求
     * @return 用户的草稿列表
     */
    @GetMapping("/my-drafts")
    public ResponseEntity<ApiResponse<Page<ArticleDraft>>> getUserDrafts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        
        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "请先登录"));
        }
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
            Page<ArticleDraft> drafts = articleDraftService.getUserDrafts(currentUser.getUserId(), pageable);
            return ResponseEntity.ok(ApiResponse.success(drafts, "获取用户草稿列表成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "获取草稿列表失败: " + e.getMessage()));
        }
    }

    /**
     * 根据状态获取草稿列表（仅管理员）
     * @param status 审核状态
     * @param page 页码
     * @param size 每页大小
     * @param httpRequest HTTP请求
     * @return 指定状态的草稿列表
     */
    @GetMapping("/by-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<Page<ArticleDraft>>> getDraftsByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        
        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "请先登录"));
        }
        
        // 检查是否为管理员
        if (isUserAdmin(currentUser)) {
            return ResponseEntity.ok(ApiResponse.error(403, "只有管理员可以按状态查看草稿"));
        }
        
        try {
            ArticleDraft.ReviewStatus reviewStatus = ArticleDraft.ReviewStatus.valueOf(status.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
            Page<ArticleDraft> drafts = articleDraftService.getDraftsByStatus(reviewStatus, pageable);
            return ResponseEntity.ok(ApiResponse.success(drafts, "获取草稿列表成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(400, "无效的审核状态: " + status));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "获取草稿列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取草稿详情
     * @param draftId 草稿ID
     * @param httpRequest HTTP请求
     * @return 草稿详情
     */
    @GetMapping("/{draftId}")
    public ResponseEntity<ApiResponse<ArticleDraft>> getDraftById(
            @PathVariable Long draftId,
            HttpServletRequest httpRequest) {
        
        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "请先登录"));
        }
        
        try {
            Optional<ArticleDraft> draftOpt = articleDraftService.getDraftById(draftId);
            if (draftOpt.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error(404, "草稿不存在"));
            }
            
            ArticleDraft draft = draftOpt.get();
            
            // 检查权限：只有草稿作者或管理员可以查看
            if (!draft.getEditorUserId().equals(currentUser.getUserId()) &&
                    isUserAdmin(currentUser)) {
                return ResponseEntity.ok(ApiResponse.error(403, "无权限查看此草稿"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(draft, "获取草稿详情成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "获取草稿详情失败: " + e.getMessage()));
        }
    }

    /**
     * 删除草稿
     * @param draftId 草稿ID
     * @param httpRequest HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{draftId}")
    public ResponseEntity<ApiResponse<Void>> deleteDraft(
            @PathVariable Long draftId,
            HttpServletRequest httpRequest) {
        
        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "请先登录"));
        }
        
        try {
            articleDraftService.deleteDraft(draftId, currentUser.getUserId());
            return ResponseEntity.ok(ApiResponse.success(null, "草稿删除成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "删除草稿失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有草稿列表（仅管理员）
     * @param page 页码
     * @param size 每页大小
     * @param httpRequest HTTP请求
     * @return 所有草稿列表
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<Page<ArticleDraft>>> getAllDrafts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        
        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "请先登录"));
        }
        
        // 检查是否为管理员
        if (isUserAdmin(currentUser)) {
            return ResponseEntity.ok(ApiResponse.error(403, "只有管理员可以查看所有草稿"));
        }
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
            Page<ArticleDraft> drafts = articleDraftService.getAllDrafts(pageable);
            return ResponseEntity.ok(ApiResponse.success(drafts, "获取所有草稿列表成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "获取草稿列表失败: " + e.getMessage()));
        }
    }
}
