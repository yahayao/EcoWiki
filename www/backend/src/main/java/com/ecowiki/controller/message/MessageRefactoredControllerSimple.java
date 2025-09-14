/**
 * 改进的消息API控制器类 - 简化版本
 * 
 * 功能：
 * - 提供RESTful消息管理接口
 * - 支持多种消息类型和操作
 * - 包含完整的权限验证和异常处理
 * - 提供统一的API响应格式
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.controller.message;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.dto.message.MessageRefactoredDto;
import com.ecowiki.dto.message.SendMessageRefactoredRequest;
import com.ecowiki.dto.message.BroadcastMessageRefactoredRequest;
import com.ecowiki.entity.user.User;
import com.ecowiki.enums.message.MessageType;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.message.MessageService;
import com.ecowiki.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController("messageRefactoredControllerSimple")
@RequestMapping("/api/messages")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:5173"}, allowCredentials = "true")
@Validated
public class MessageRefactoredControllerSimple {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageRefactoredControllerSimple.class);
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // ===== 消息发送接口 =====
    
    /**
     * 发送消息
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageRefactoredDto>> sendMessage(
            @Valid @RequestBody SendMessageRefactoredRequest request,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            MessageRefactoredDto message = messageService.sendMessage(
                currentUser.getUserId().intValue(),
                request.getRecipientUserId(),
                request.getContent(),
                request.getSubject(),
                request.getMessageType() != null ? request.getMessageType() : MessageType.USER_PRIVATE,
                request.getMetadata(),
                request.getExpireTime()
            );
            
            logger.info("消息发送成功: 发送者={}, 接收者={}, 类型={}", 
                       currentUser.getUserId(), request.getRecipientUserId(), message.getMessageType());
            
            return ResponseEntity.ok(ApiResponse.success(message, "消息发送成功"));
        } catch (Exception e) {
            logger.error("发送消息失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("发送失败: " + e.getMessage()));
        }
    }
    
    /**
     * 群发消息
     */
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<List<MessageRefactoredDto>>> broadcastMessage(
            @Valid @RequestBody BroadcastMessageRefactoredRequest request,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            List<MessageRefactoredDto> messages = messageService.broadcastMessage(
                currentUser.getUserId().intValue(),
                request.getRecipientUserIds(),
                request.getContent(),
                request.getMessageType() != null ? request.getMessageType() : MessageType.USER_PRIVATE
            );
            
            logger.info("群发消息成功: 发送者={}, 成功数量={}", 
                       currentUser.getUserId(), messages.size());
            
            return ResponseEntity.ok(ApiResponse.success(messages, "消息群发成功"));
        } catch (Exception e) {
            logger.error("群发消息失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("群发失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取收到的消息
     */
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<Page<MessageRefactoredDto>>> getReceivedMessages(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<MessageRefactoredDto> messages = messageService.getReceivedMessages(
                currentUser.getUserId().intValue(), pageable);
            
            return ResponseEntity.ok(ApiResponse.success(messages, "获取消息成功"));
        } catch (Exception e) {
            logger.error("获取收到消息失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            Long count = messageService.getUnreadMessageCount(currentUser.getUserId().intValue());
            return ResponseEntity.ok(ApiResponse.success(count, "获取未读数量成功"));
        } catch (Exception e) {
            logger.error("获取未读消息数量失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取未读数量失败: " + e.getMessage()));
        }
    }
    
    /**
     * 标记消息为已读
     */
    @PutMapping("/{messageId}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @PathVariable Integer messageId,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            messageService.markAsRead(messageId, currentUser.getUserId().intValue());
            return ResponseEntity.ok(ApiResponse.success(null, "标记已读成功"));
        } catch (Exception e) {
            logger.error("标记消息已读失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("标记已读失败: " + e.getMessage()));
        }
    }
    
    /**
     * 标记所有消息为已读
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            messageService.markAllAsRead(currentUser.getUserId().intValue());
            return ResponseEntity.ok(ApiResponse.success(null, "全部标记已读成功"));
        } catch (Exception e) {
            logger.error("批量标记已读失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("标记已读失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<String>> deleteMessage(
            @PathVariable Integer messageId,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            messageService.deleteMessage(messageId, currentUser.getUserId().intValue());
            return ResponseEntity.ok(ApiResponse.success(null, "删除消息成功"));
        } catch (Exception e) {
            logger.error("删除消息失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除消息失败: " + e.getMessage()));
        }
    }
    
    // ===== 工具方法 =====
    
    /**
     * 从请求中获取当前用户
     */
    private User getCurrentUser(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) return null;
            
            String username = jwtUtil.extractUsername(token);
            if (username == null || !jwtUtil.validateToken(token, username)) {
                return null;
            }
            
            Optional<User> userOpt = userService.findByUsername(username);
            return userOpt.orElse(null);
        } catch (Exception e) {
            logger.warn("获取当前用户失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从请求头提取JWT Token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}