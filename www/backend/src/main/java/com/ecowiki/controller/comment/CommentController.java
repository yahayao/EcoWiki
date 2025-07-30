package com.ecowiki.controller.comment;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.dto.comment.CommentDTO;
import com.ecowiki.dto.comment.ReplyDTO;
import com.ecowiki.service.CommentService;
import com.ecowiki.service.CommentService.CommentStats;
import com.ecowiki.service.CommentService.LikeResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 评论控制器
 * 
 * 处理评论相关的HTTP请求
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2025-01-21
 */
@RestController
@RequestMapping("/api/comments")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true", maxAge = 3600)
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    /**
     * 获取文章的评论列表
     * 
     * @param articleId 文章ID
     * @param page 页码，默认0
     * @param size 每页大小，默认20
     * @param sort 排序方式：newest(最新)、oldest(最早)、hot(最热)，默认newest
     * @param authentication 认证信息
     * @return 评论分页列表
     */
    @GetMapping("/article/{articleId}")
    public ResponseEntity<ApiResponse<Page<CommentDTO>>> getComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "newest") String sort,
            Authentication authentication) {
        
        String currentUser = authentication != null ? authentication.getName() : null;
        
        try {
            Page<CommentDTO> comments = commentService.getCommentsByArticleId(articleId, page, size, sort, currentUser);
            return ResponseEntity.ok(ApiResponse.success(comments));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取评论失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建评论
     * 
     * @param request 评论请求数据
     * @param authentication 认证信息
     * @param httpRequest HTTP请求（用于获取IP等信息）
     * @return 创建的评论
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(
            @RequestBody Map<String, Object> request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        if (authentication == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("请先登录"));
        }
        
        try {
            Long articleId = Long.valueOf(request.get("articleId").toString());
            String content = request.get("content").toString();
            String author = authentication.getName();
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("评论内容不能为空"));
            }
            
            CommentDTO comment = commentService.createComment(articleId, content, author, ipAddress, userAgent);
            return ResponseEntity.ok(ApiResponse.success(comment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("创建评论失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建回复
     * 
     * @param commentId 父评论ID
     * @param request 回复请求数据
     * @param authentication 认证信息
     * @param httpRequest HTTP请求
     * @return 创建的回复
     */
    @PostMapping("/{commentId}/reply")
    public ResponseEntity<ApiResponse<ReplyDTO>> createReply(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        if (authentication == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("请先登录"));
        }
        
        try {
            String content = request.get("content");
            String author = authentication.getName();
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("回复内容不能为空"));
            }
            
            ReplyDTO reply = commentService.createReply(commentId, content, author, ipAddress, userAgent);
            return ResponseEntity.ok(ApiResponse.success(reply));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("创建回复失败: " + e.getMessage()));
        }
    }
    
    /**
     * 切换评论点赞状态
     * 
     * @param commentId 评论ID
     * @param authentication 认证信息
     * @return 点赞结果
     */
    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<LikeResult>> toggleCommentLike(
            @PathVariable Long commentId,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("请先登录"));
        }
        
        try {
            String userName = authentication.getName();
            LikeResult result = commentService.toggleCommentLike(commentId, userName);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("操作失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除评论
     * 
     * @param commentId 评论ID
     * @param authentication 认证信息
     * @return 删除结果
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("请先登录"));
        }
        
        try {
            String userName = authentication.getName();
            commentService.deleteComment(commentId, userName);
            return ResponseEntity.ok(ApiResponse.success("评论已删除"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除回复
     * 
     * @param replyId 回复ID
     * @param authentication 认证信息
     * @return 删除结果
     */
    @DeleteMapping("/reply/{replyId}")
    public ResponseEntity<ApiResponse<String>> deleteReply(
            @PathVariable Long replyId,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("请先登录"));
        }
        
        try {
            String userName = authentication.getName();
            commentService.deleteReply(replyId, userName);
            return ResponseEntity.ok(ApiResponse.success("回复已删除"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("删除回复失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取文章评论统计
     * 
     * @param articleId 文章ID
     * @return 评论统计信息
     */
    @GetMapping("/article/{articleId}/stats")
    public ResponseEntity<ApiResponse<CommentStats>> getCommentStats(@PathVariable Long articleId) {
        try {
            CommentStats stats = commentService.getCommentStats(articleId);
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取统计失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0];
        }
        return ipAddress;
    }
}
