package com.ecowiki.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.entity.ArticleReview;
import com.ecowiki.entity.ReviewHistory;
import com.ecowiki.entity.ReviewerAssignment;
import com.ecowiki.service.ArticleReviewService;
import com.ecowiki.service.PermissionService;
import com.ecowiki.service.UserService;

/**
 * 文章审核控制器
 * 
 * 提供文章审核相关的REST API接口
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-07-25
 */
@RestController
@RequestMapping("/api/review")
@CrossOrigin(origins = "*")
public class ArticleReviewController {
    
    private static final Logger logger = LoggerFactory.getLogger(ArticleReviewController.class);
    
    @Autowired
    private ArticleReviewService reviewService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PermissionService permissionService;
    
    /**
     * 创建审核申请
     * 
     * @param request 审核申请参数
     * @return 创建的审核记录
     */
    @PostMapping("/create")
    public ResponseEntity<?> createReview(@RequestBody CreateReviewRequest request) {
        try {
            ArticleReview review = reviewService.createReview(
                request.getArticleId(),
                request.getSubmitterId(),
                request.getReviewType(),
                request.getContentSnapshot(),
                request.getAutoPublish()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "审核申请创建成功",
                "data", review
            ));
        } catch (Exception e) {
            logger.error("创建审核申请失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 分配审核员
     * 
     * @param reviewId 审核ID
     * @param request 分配参数
     * @return 分配结果
     */
    @PostMapping("/{reviewId}/assign")
    public ResponseEntity<?> assignReviewer(
            @PathVariable Long reviewId,
            @RequestBody AssignReviewerRequest request) {
        try {
            ReviewerAssignment assignment = reviewService.assignReviewer(
                reviewId,
                request.getReviewerId(),
                request.getAssignerId(),
                request.getReason()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "审核员分配成功",
                "data", assignment
            ));
        } catch (Exception e) {
            logger.error("分配审核员失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 处理审核
     * 
     * @param reviewId 审核ID
     * @param request 审核参数
     * @return 审核结果
     */
    @PostMapping("/{reviewId}/process")
    public ResponseEntity<?> processReview(
            @PathVariable Long reviewId,
            @RequestBody ProcessReviewRequest request) {
        try {
            ArticleReview review = reviewService.processReview(
                reviewId,
                request.getReviewerId(),
                request.getApproved(),
                request.getReason()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "审核处理成功",
                "data", review
            ));
        } catch (Exception e) {
            logger.error("处理审核失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取审核详情
     * 
     * @param reviewId 审核ID
     * @return 审核详情
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewDetail(@PathVariable Long reviewId) {
        try {
            ArticleReview review = reviewService.getReviewById(reviewId);
            List<ReviewHistory> history = reviewService.getReviewHistory(reviewId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("review", review);
            result.put("history", history);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result
            ));
        } catch (Exception e) {
            logger.error("获取审核详情失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取用户待审核列表
     * 
     * @param reviewerId 审核员ID
     * @param page 页码
     * @param size 页大小
     * @return 待审核列表
     */
    @GetMapping("/pending/{reviewerId}")
    public ResponseEntity<?> getPendingReviews(
            @PathVariable Long reviewerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("submitTime").descending());
            Page<ArticleReview> reviews = reviewService.getPendingReviews(reviewerId, pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reviews
            ));
        } catch (Exception e) {
            logger.error("获取待审核列表失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取所有待审核列表（管理员）
     * 
     * @param page 页码
     * @param size 页大小
     * @return 待审核列表
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getAllPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("submitTime").descending());
            Page<ArticleReview> reviews = reviewService.getAllPendingReviews(pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reviews
            ));
        } catch (Exception e) {
            logger.error("获取所有待审核列表失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取审核统计
     * 
     * @param reviewerId 审核员ID
     * @param days 统计天数
     * @return 统计信息
     */
    @GetMapping("/statistics/{reviewerId}")
    public ResponseEntity<?> getReviewStatistics(
            @PathVariable Long reviewerId,
            @RequestParam(defaultValue = "30") int days) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);
            
            Map<String, Object> statistics = reviewService.getReviewStatistics(
                reviewerId, startTime, endTime);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", statistics
            ));
        } catch (Exception e) {
            logger.error("获取审核统计失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // ==================== 请求参数类 ====================
    
    /**
     * 创建审核请求参数
     */
    public static class CreateReviewRequest {
        private Long articleId;
        private Long submitterId;
        private ArticleReview.ReviewType reviewType;
        private String contentSnapshot;
        private Boolean autoPublish;
        
        // Getters and Setters
        public Long getArticleId() {
            return articleId;
        }
        
        public void setArticleId(Long articleId) {
            this.articleId = articleId;
        }
        
        public Long getSubmitterId() {
            return submitterId;
        }
        
        public void setSubmitterId(Long submitterId) {
            this.submitterId = submitterId;
        }
        
        public ArticleReview.ReviewType getReviewType() {
            return reviewType;
        }
        
        public void setReviewType(ArticleReview.ReviewType reviewType) {
            this.reviewType = reviewType;
        }
        
        public String getContentSnapshot() {
            return contentSnapshot;
        }
        
        public void setContentSnapshot(String contentSnapshot) {
            this.contentSnapshot = contentSnapshot;
        }
        
        public Boolean getAutoPublish() {
            return autoPublish;
        }
        
        public void setAutoPublish(Boolean autoPublish) {
            this.autoPublish = autoPublish;
        }
    }
    
    /**
     * 分配审核员请求参数
     */
    public static class AssignReviewerRequest {
        private Long reviewerId;
        private Long assignerId;
        private String reason;
        
        // Getters and Setters
        public Long getReviewerId() {
            return reviewerId;
        }
        
        public void setReviewerId(Long reviewerId) {
            this.reviewerId = reviewerId;
        }
        
        public Long getAssignerId() {
            return assignerId;
        }
        
        public void setAssignerId(Long assignerId) {
            this.assignerId = assignerId;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
    
    /**
     * 处理审核请求参数
     */
    public static class ProcessReviewRequest {
        private Long reviewerId;
        private Boolean approved;
        private String reason;
        
        // Getters and Setters
        public Long getReviewerId() {
            return reviewerId;
        }
        
        public void setReviewerId(Long reviewerId) {
            this.reviewerId = reviewerId;
        }
        
        public Boolean getApproved() {
            return approved;
        }
        
        public void setApproved(Boolean approved) {
            this.approved = approved;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
