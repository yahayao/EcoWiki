package com.ecowiki.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.entity.ArticleReview;
import com.ecowiki.entity.ReviewHistory;
import com.ecowiki.entity.ReviewPermissionConfig;
import com.ecowiki.entity.ReviewerAssignment;
import com.ecowiki.entity.User;
import com.ecowiki.repository.ArticleReviewRepository;
import com.ecowiki.repository.ReviewHistoryRepository;
import com.ecowiki.repository.ReviewPermissionConfigRepository;
import com.ecowiki.repository.ReviewerAssignmentRepository;
import com.ecowiki.repository.UserRepository;

/**
 * 文章审核服务类
 * 
 * 提供文章审核的核心业务逻辑，包括审核申请、分配、处理等功能
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-07-25
 */
@Service
@Transactional
public class ArticleReviewService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticleReviewService.class);
    
    @Autowired
    private ArticleReviewRepository articleReviewRepository;
    
    @Autowired
    private ReviewPermissionConfigRepository permissionConfigRepository;
    
    @Autowired
    private ReviewerAssignmentRepository assignmentRepository;
    
    @Autowired
    private ReviewHistoryRepository historyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private PermissionService permissionService;
    
    /**
     * 创建审核申请
     * 
     * @param articleId 文章ID
     * @param submitterId 提交人ID
     * @param reviewType 审核类型
     * @param contentSnapshot 内容快照
     * @param autoPublish 是否自动发布
     * @return 审核记录
     */
    public ArticleReview createReview(Long articleId, Long submitterId, ArticleReview.ReviewType reviewType,
                                      String contentSnapshot, Boolean autoPublish) {
        // 检查是否已有进行中的审核
        if (articleReviewRepository.hasActivePendingReview(articleId)) {
            throw new RuntimeException("该文章已有正在进行的审核，请等待审核完成");
        }
        
        // 创建审核记录
        ArticleReview review = new ArticleReview(articleId, submitterId, reviewType);
        review.setContentSnapshot(contentSnapshot);
        review.setAutoPublish(autoPublish != null ? autoPublish : false);
        
        // 设置优先级和截止时间
        setPriorityAndDeadline(review);
        
        // 保存审核记录
        review = articleReviewRepository.save(review);
        
        // 记录历史
        recordHistory(review.getReviewId(), submitterId, ReviewHistory.ActionType.CREATED, 
                     null, ArticleReview.ReviewStatus.PENDING, "创建审核申请");
        
        // 自动分配审核员
        autoAssignReviewer(review);
        
        logger.info("创建审核申请成功: reviewId={}, articleId={}, reviewType={}", 
                   review.getReviewId(), articleId, reviewType);
        
        return review;
    }
    
    /**
     * 手动分配审核员
     * 
     * @param reviewId 审核ID
     * @param reviewerId 审核员ID
     * @param assignerId 分配员ID
     * @param reason 分配原因
     * @return 分配记录
     */
    public ReviewerAssignment assignReviewer(Long reviewId, Long reviewerId, Long assignerId, String reason) {
        ArticleReview review = getReviewById(reviewId);
        
        // 检查分配权限
        if (!canAssignReviewer(assignerId)) {
            throw new RuntimeException("您没有权限分配该类型的审核");
        }
        
        // 检查审核员是否有审核权限
        if (!canReview(reviewerId, review.getReviewType())) {
            throw new RuntimeException("指定的审核员没有该类型的审核权限");
        }
        
        // 检查是否已分配给该审核员
        if (assignmentRepository.isAssignedToReviewer(reviewId, reviewerId)) {
            throw new RuntimeException("该审核已分配给指定的审核员");
        }
        
        // 检查审核员工作负载
        if (isReviewerOverloaded(reviewerId)) {
            throw new RuntimeException("审核员工作负载过重，请选择其他审核员");
        }
        
        // 创建分配记录
        ReviewerAssignment assignment = new ReviewerAssignment(reviewId, reviewerId, assignerId, false);
        assignment.setAssignmentReason(reason);
        assignment.setExpectedCompletionTime(calculateExpectedCompletionTime(review));
        assignment = assignmentRepository.save(assignment);
        
        // 更新审核记录
        review.setReviewerId(reviewerId);
        articleReviewRepository.save(review);
        
        // 记录历史
        recordHistory(reviewId, assignerId, ReviewHistory.ActionType.ASSIGNED, 
                     null, null, "手动分配审核员: " + getUserName(reviewerId));
        
        // 发送通知
        sendReviewAssignmentNotification(reviewerId, review);
        
        logger.info("手动分配审核员成功: reviewId={}, reviewerId={}, assignerId={}", 
                   reviewId, reviewerId, assignerId);
        
        return assignment;
    }
    
    /**
     * 自动分配审核员
     * 
     * @param review 审核记录
     */
    private void autoAssignReviewer(ArticleReview review) {
        try {
            List<ReviewPermissionConfig> configs = permissionConfigRepository
                .findAutoAssignmentConfigsByWeight(review.getReviewType());
            
            if (configs.isEmpty()) {
                logger.warn("没有找到自动分配配置: reviewType={}", review.getReviewType());
                return;
            }
            
            List<Long> eligibleReviewers = findEligibleReviewers(configs, review);
            
            if (eligibleReviewers.isEmpty()) {
                logger.warn("没有找到合适的审核员: reviewId={}", review.getReviewId());
                sendNoReviewerNotification(review);
                return;
            }
            
            // 选择最合适的审核员
            Long selectedReviewerId = selectBestReviewer(eligibleReviewers);
            
            // 创建自动分配记录
            ReviewerAssignment assignment = new ReviewerAssignment(
                review.getReviewId(), selectedReviewerId, null, true);
            assignment.setExpectedCompletionTime(calculateExpectedCompletionTime(review));
            assignment.setWeightScore(calculateReviewerScore(selectedReviewerId));
            assignmentRepository.save(assignment);
            
            // 更新审核记录
            review.setReviewerId(selectedReviewerId);
            articleReviewRepository.save(review);
            
            // 记录历史
            recordHistory(review.getReviewId(), selectedReviewerId, ReviewHistory.ActionType.ASSIGNED,
                         null, null, "自动分配审核员: " + getUserName(selectedReviewerId));
            
            // 发送通知
            sendReviewAssignmentNotification(selectedReviewerId, review);
            
            logger.info("自动分配审核员成功: reviewId={}, reviewerId={}", 
                       review.getReviewId(), selectedReviewerId);
            
        } catch (Exception e) {
            logger.error("自动分配审核员失败: reviewId={}", review.getReviewId(), e);
        }
    }
    
    /**
     * 处理审核结果
     * 
     * @param reviewId 审核ID
     * @param reviewerId 审核员ID
     * @param approved 是否通过
     * @param reason 审核理由
     * @return 处理后的审核记录
     */
    public ArticleReview processReview(Long reviewId, Long reviewerId, boolean approved, String reason) {
        ArticleReview review = getReviewById(reviewId);
        
        // 验证审核权限
        if (!Objects.equals(review.getReviewerId(), reviewerId)) {
            throw new RuntimeException("您不是该审核的指定审核员");
        }
        
        if (review.getStatus() != ArticleReview.ReviewStatus.PENDING) {
            throw new RuntimeException("该审核已经处理完成");
        }
        
        // 更新审核状态
        ArticleReview.ReviewStatus oldStatus = review.getStatus();
        ArticleReview.ReviewStatus newStatus = approved ? 
            ArticleReview.ReviewStatus.APPROVED : ArticleReview.ReviewStatus.REJECTED;
        
        review.setStatus(newStatus);
        review.setReviewReason(reason);
        review.setReviewTime(LocalDateTime.now());
        review = articleReviewRepository.save(review);
        
        // 更新分配状态
        updateAssignmentStatus(reviewId, reviewerId, ReviewerAssignment.AssignmentStatus.COMPLETED);
        
        // 记录历史
        recordHistory(reviewId, reviewerId, 
                     approved ? ReviewHistory.ActionType.APPROVED : ReviewHistory.ActionType.REJECTED,
                     oldStatus, newStatus, reason);
        
        // 处理审核结果
        handleReviewResult(review, approved);
        
        logger.info("处理审核结果成功: reviewId={}, approved={}, reviewerId={}", 
                   reviewId, approved, reviewerId);
        
        return review;
    }
    
    /**
     * 处理审核结果后续操作
     */
    private void handleReviewResult(ArticleReview review, boolean approved) {
        if (approved) {
            // 审核通过的处理
            if (review.getAutoPublish()) {
                // 自动发布文章
                try {
                    switch (review.getReviewType()) {
                        case CREATE, UPDATE -> // 这里应该调用文章服务的发布方法，暂时注释
                            // articleService.publishArticle(review.getArticleId());
                            logger.info("文章审核通过，需要手动发布: articleId={}", review.getArticleId());
                        case DELETE -> // 这里应该调用文章服务的删除方法，暂时注释
                            // articleService.deleteArticle(review.getArticleId());
                            logger.info("文章删除审核通过，需要手动删除: articleId={}", review.getArticleId());
                    }
                } catch (Exception e) {
                    logger.error("自动发布文章失败: articleId={}", review.getArticleId(), e);
                }
            }
            
            // 发送通过通知
            sendReviewResultNotification(review, true);
        } else {
            // 审核拒绝的处理
            sendReviewResultNotification(review, false);
        }
    }
    
    /**
     * 获取审核详情
     * 
     * @param reviewId 审核ID
     * @return 审核记录
     */
    @Transactional(readOnly = true)
    public ArticleReview getReviewById(Long reviewId) {
        return articleReviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("审核记录不存在: " + reviewId));
    }
    
    /**
     * 获取用户的待审核列表
     * 
     * @param reviewerId 审核员ID
     * @param pageable 分页参数
     * @return 待审核列表
     */
    @Transactional(readOnly = true)
    public Page<ArticleReview> getPendingReviews(Long reviewerId, Pageable pageable) {
        List<ArticleReview> reviews = articleReviewRepository.findByReviewerIdAndStatus(
            reviewerId, ArticleReview.ReviewStatus.PENDING);
        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), reviews.size());
        List<ArticleReview> pageContent = reviews.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, reviews.size());
    }
    
    /**
     * 获取所有待审核列表（管理员用）
     * 
     * @param pageable 分页参数
     * @return 待审核列表
     */
    @Transactional(readOnly = true)
    public Page<ArticleReview> getAllPendingReviews(Pageable pageable) {
        return articleReviewRepository.findByStatus(ArticleReview.ReviewStatus.PENDING, pageable);
    }
    
    /**
     * 获取审核历史
     * 
     * @param reviewId 审核ID
     * @return 历史记录列表
     */
    @Transactional(readOnly = true)
    public List<ReviewHistory> getReviewHistory(Long reviewId) {
        return historyRepository.findByReviewIdOrderByActionTimeDesc(reviewId);
    }
    
    /**
     * 获取审核统计信息
     * 
     * @param reviewerId 审核员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getReviewStatistics(Long reviewerId, LocalDateTime startTime, LocalDateTime endTime) {
        Object[] stats = articleReviewRepository.getReviewerStatistics(reviewerId, startTime, endTime);
        
        Map<String, Object> result = new HashMap<>();
        if (stats != null && stats.length > 0) {
            result.put("totalReviews", stats[0]);
            result.put("approvedCount", stats[1]);
            result.put("rejectedCount", stats[2]);
            result.put("avgReviewTimeHours", stats[3]);
        }
        
        // 当前待审核数量
        Long pendingCount = articleReviewRepository.countPendingReviewsByReviewer(reviewerId);
        result.put("pendingCount", pendingCount);
        
        return result;
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 设置优先级和截止时间
     */
    private void setPriorityAndDeadline(ArticleReview review) {
        // 根据审核类型设置默认优先级
        switch (review.getReviewType()) {
            case DELETE:
                review.setPriorityLevel(3);
                review.setReviewDeadline(LocalDateTime.now().plusHours(24));
                break;
            case UPDATE:
                review.setPriorityLevel(2);
                review.setReviewDeadline(LocalDateTime.now().plusHours(48));
                break;
            case CREATE:
            default:
                review.setPriorityLevel(1);
                review.setReviewDeadline(LocalDateTime.now().plusHours(72));
                break;
        }
    }
    
    /**
     * 记录操作历史
     */
    private void recordHistory(Long reviewId, Long operatorId, ReviewHistory.ActionType actionType,
                              ArticleReview.ReviewStatus oldStatus, ArticleReview.ReviewStatus newStatus,
                              String description) {
        ReviewHistory history = new ReviewHistory(reviewId, operatorId, actionType, oldStatus, newStatus);
        history.setActionDescription(description);
        historyRepository.save(history);
    }
    
    /**
     * 检查是否可以分配审核员
     */
    private boolean canAssignReviewer(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return permissionService.isAdmin(user) || permissionService.isSuperAdmin(user);
    }
    
    /**
     * 检查是否可以审核
     */
    private boolean canReview(Long userId, ArticleReview.ReviewType reviewType) {
        return !permissionConfigRepository.findByRoleNamesAndReviewType(
            getUserRoleNames(userId), reviewType).isEmpty();
    }
    
    /**
     * 获取用户角色名称列表
     */
    private List<String> getUserRoleNames(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return new ArrayList<>();
        
        List<String> roles = new ArrayList<>();
        if (permissionService.isSuperAdmin(user)) {
            roles.add("superadmin");
        } else if (permissionService.isAdmin(user)) {
            roles.add("admin");
        } else {
            roles.add("user");
        }
        return roles;
    }
    
    /**
     * 检查审核员是否过载
     */
    private boolean isReviewerOverloaded(Long reviewerId) {
        Long activeCount = assignmentRepository.countActiveAssignmentsByReviewer(reviewerId);
        // 简单设置最大同时审核数为10
        return activeCount >= 10;
    }
    
    /**
     * 查找符合条件的审核员
     */
    private List<Long> findEligibleReviewers(List<ReviewPermissionConfig> configs, ArticleReview review) {
        Set<String> eligibleRoles = configs.stream()
            .filter(config -> config.getPriorityLevel() >= review.getPriorityLevel())
            .map(ReviewPermissionConfig::getRoleName)
            .collect(Collectors.toSet());
        
        // 查找具有指定角色的用户
        List<User> users = userRepository.findAll();
        return users.stream()
            .filter(user -> {
                List<String> userRoles = getUserRoleNames(user.getUserId());
                return userRoles.stream().anyMatch(eligibleRoles::contains);
            })
            .map(User::getUserId)
            .filter(userId -> !isReviewerOverloaded(userId))
            .collect(Collectors.toList());
    }
    
    /**
     * 选择最佳审核员
     */
    private Long selectBestReviewer(List<Long> eligibleReviewers) {
        return eligibleReviewers.stream()
            .min(Comparator.comparing(reviewerId -> 
                assignmentRepository.countActiveAssignmentsByReviewer(reviewerId)))
            .orElse(eligibleReviewers.get(0));
    }
    
    /**
     * 计算审核员分数
     */
    private Double calculateReviewerScore(Long reviewerId) {
        Long activeCount = assignmentRepository.countActiveAssignmentsByReviewer(reviewerId);
        return 100.0 - activeCount * 10.0; // 简单的评分算法
    }
    
    /**
     * 计算预期完成时间
     */
    private LocalDateTime calculateExpectedCompletionTime(ArticleReview review) {
        return review.getReviewDeadline() != null ? 
            review.getReviewDeadline().minusHours(2) : LocalDateTime.now().plusHours(24);
    }
    
    /**
     * 更新分配状态
     */
    private void updateAssignmentStatus(Long reviewId, Long reviewerId, 
                                       ReviewerAssignment.AssignmentStatus status) {
        List<ReviewerAssignment> assignments = assignmentRepository
            .findByReviewIdAndStatus(reviewId, ReviewerAssignment.AssignmentStatus.ACTIVE);
        
        assignments.stream()
            .filter(assignment -> Objects.equals(assignment.getReviewerId(), reviewerId))
            .forEach(assignment -> {
                assignment.setStatus(status);
                assignmentRepository.save(assignment);
            });
    }
    
    /**
     * 发送审核分配通知
     */
    private void sendReviewAssignmentNotification(Long reviewerId, ArticleReview review) {
        try {
            String title = "新的审核任务";
            String content = String.format("您有一个新的%s审核任务，文章ID: %d，请及时处理。",
                review.getReviewType().getDescription(), review.getArticleId());
            
            messageService.sendMessage(null, reviewerId.intValue(), title, content);
        } catch (Exception e) {
            logger.error("发送审核分配通知失败", e);
        }
    }
    
    /**
     * 发送审核结果通知
     */
    private void sendReviewResultNotification(ArticleReview review, boolean approved) {
        try {
            String title = approved ? "审核通过通知" : "审核拒绝通知";
            String content = String.format("您的文章(ID: %d)审核%s。%s",
                review.getArticleId(),
                approved ? "已通过" : "被拒绝",
                review.getReviewReason() != null ? "原因: " + review.getReviewReason() : "");
            
            messageService.sendMessage(null, review.getSubmitterId().intValue(), title, content);
        } catch (Exception e) {
            logger.error("发送审核结果通知失败", e);
        }
    }
    
    /**
     * 发送无审核员通知
     */
    private void sendNoReviewerNotification(ArticleReview review) {
        try {
            List<User> adminUsers = getAdminUsers();
            String title = "审核分配失败";
            String content = String.format("审核ID: %d 无法自动分配审核员，请手动处理。", review.getReviewId());
            
            for (User admin : adminUsers) {
                messageService.sendMessage(null, admin.getUserId().intValue(), title, content);
            }
        } catch (Exception e) {
            logger.error("发送无审核员通知失败", e);
        }
    }
    
    /**
     * 获取管理员用户列表
     */
    private List<User> getAdminUsers() {
        return userRepository.findAll().stream()
            .filter(user -> permissionService.isAdmin(user) || permissionService.isSuperAdmin(user))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取用户名称
     */
    private String getUserName(Long userId) {
        return userRepository.findById(userId)
            .map(User::getUsername)
            .orElse("未知用户");
    }
}
