/**
 * 改进的消息API控制器类
 * 
 * 功能：
 * - 提供RESTful消息管理接口
 * - 支持多种消息类型和操作
 * - 包含完整的权限验证和异常处理
 * - 提供统一的API响应格式
 * - 支持消息的发送、接收、标记、删除等操作
 * - 支持分页查询和条件筛选
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.controller.message;

import java.util.List;
import java.util.Map;
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
import com.ecowiki.enums.message.MessageStatus;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.message.MessageService;
import com.ecowiki.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController("messageRefactoredController")
@RequestMapping("/messages")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@Validated
public class MessageRefactoredController {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageRefactoredController.class);
    
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
    
    // ===== 消息查询接口 =====
    
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
            
            logger.debug("获取收到消息成功: 用户={}, 页码={}, 数量={}", 
                        currentUser.getUserId(), page, messages.getNumberOfElements());
            
            return ResponseEntity.ok(ApiResponse.success(messages, "获取消息成功"));
        } catch (Exception e) {
            logger.error("获取收到消息失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取发送的消息
     */
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<Page<MessageRefactoredDto>>> getSentMessages(
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
            Page<MessageRefactoredDto> messages = messageService.getSentMessages(
                currentUser.getUserId().intValue(), pageable);
            
            logger.debug("获取发送消息成功: 用户={}, 页码={}, 数量={}", 
                        currentUser.getUserId(), page, messages.getNumberOfElements());
            
            return ResponseEntity.ok(ApiResponse.success(messages, "获取消息成功"));
        } catch (Exception e) {
            logger.error("获取发送消息失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有消息（收发消息合并）
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<MessageRefactoredDto>>> getAllMessages(
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
            Page<MessageRefactoredDto> messages = messageService.getUserMessages(
                currentUser.getUserId().intValue(), pageable);
            
            logger.info("获取所有消息成功: 用户={}, 页码={}, 数量={}", 
                       currentUser.getUserId(), page, messages.getNumberOfElements());
            
            return ResponseEntity.ok(ApiResponse.success(messages, "获取消息成功"));
        } catch (Exception e) {
            logger.error("获取所有消息失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取与指定用户的对话
     */
    @GetMapping("/conversation/{userId}")
    public ResponseEntity<ApiResponse<Page<MessageRefactoredDto>>> getConversation(
            @PathVariable Integer userId,
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
            Page<MessageRefactoredDto> messages = messageService.getConversation(
                currentUser.getUserId().intValue(), userId, pageable);
            
            logger.debug("获取对话成功: 用户1={}, 用户2={}, 页码={}, 数量={}", 
                        currentUser.getUserId(), userId, page, messages.getNumberOfElements());
            
            return ResponseEntity.ok(ApiResponse.success(messages, "获取对话成功"));
        } catch (Exception e) {
            logger.error("获取对话失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取对话失败: " + e.getMessage()));
        }
    }
    
    // ===== 未读消息接口 =====
    
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
            
            logger.debug("获取未读消息数量成功: 用户={}, 数量={}", currentUser.getUserId(), count);
            
            return ResponseEntity.ok(ApiResponse.success(count, "获取未读数量成功"));
        } catch (Exception e) {
            logger.error("获取未读消息数量失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取未读数量失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取未读消息列表
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<MessageRefactoredDto>>> getUnreadMessages(
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            List<MessageRefactoredDto> messages = messageService.getUnreadMessages(
                currentUser.getUserId().intValue());
            
            logger.debug("获取未读消息列表成功: 用户={}, 数量={}", 
                        currentUser.getUserId(), messages.size());
            
            return ResponseEntity.ok(ApiResponse.success(messages, "获取未读消息成功"));
        } catch (Exception e) {
            logger.error("获取未读消息列表失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取未读消息失败: " + e.getMessage()));
        }
    }
    
    // ===== 消息操作接口 =====
    
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
            
            logger.debug("标记消息已读成功: 用户={}, 消息ID={}", 
                        currentUser.getUserId(), messageId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "标记已读成功"));
        } catch (Exception e) {
            logger.error("标记消息已读失败: messageId={}", messageId, e);
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
            
            logger.info("标记所有消息已读成功: 用户={}", currentUser.getUserId());
            
            return ResponseEntity.ok(ApiResponse.success(null, "全部标记已读成功"));
        } catch (Exception e) {
            logger.error("批量标记已读失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("标记已读失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量标记消息为已读
     */
    @PutMapping("/batch/read")
    public ResponseEntity<ApiResponse<String>> batchMarkAsRead(
            @RequestBody List<Integer> messageIds,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            if (messageIds == null || messageIds.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("消息ID列表不能为空"));
            }
            
            messageService.batchMarkAsRead(messageIds, currentUser.getUserId().intValue());
            
            logger.info("批量标记消息已读成功: 用户={}, 数量={}", 
                       currentUser.getUserId(), messageIds.size());
            
            return ResponseEntity.ok(ApiResponse.success(null, "批量标记已读成功"));
        } catch (Exception e) {
            logger.error("批量标记已读失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("批量标记已读失败: " + e.getMessage()));
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
            
            logger.info("删除消息成功: 用户={}, 消息ID={}", 
                       currentUser.getUserId(), messageId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "删除消息成功"));
        } catch (Exception e) {
            logger.error("删除消息失败: messageId={}", messageId, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量删除消息
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<String>> batchDeleteMessages(
            @RequestBody List<Integer> messageIds,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            if (messageIds == null || messageIds.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("消息ID列表不能为空"));
            }
            
            messageService.batchDeleteMessages(messageIds, currentUser.getUserId().intValue());
            
            logger.info("批量删除消息成功: 用户={}, 数量={}", 
                       currentUser.getUserId(), messageIds.size());
            
            return ResponseEntity.ok(ApiResponse.success(null, "批量删除消息成功"));
        } catch (Exception e) {
            logger.error("批量删除消息失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("批量删除消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 撤回消息
     */
    @PutMapping("/{messageId}/recall")
    public ResponseEntity<ApiResponse<String>> recallMessage(
            @PathVariable Integer messageId,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            messageService.recallMessage(messageId, currentUser.getUserId().intValue());
            
            logger.info("撤回消息成功: 用户={}, 消息ID={}", 
                       currentUser.getUserId(), messageId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "撤回消息成功"));
        } catch (Exception e) {
            logger.error("撤回消息失败: messageId={}", messageId, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("撤回消息失败: " + e.getMessage()));
        }
    }
    
    // ===== 消息详情和统计接口 =====
    
    /**
     * 获取消息详情
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageRefactoredDto>> getMessageDetail(
            @PathVariable Integer messageId,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            MessageRefactoredDto message = messageService.getMessageDetail(
                messageId, currentUser.getUserId().intValue());
            
            logger.debug("获取消息详情成功: 用户={}, 消息ID={}", 
                        currentUser.getUserId(), messageId);
            
            return ResponseEntity.ok(ApiResponse.success(message, "获取消息详情成功"));
        } catch (Exception e) {
            logger.error("获取消息详情失败: messageId={}", messageId, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取消息详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 按类型获取消息统计
     */
    @GetMapping("/stats/types")
    public ResponseEntity<ApiResponse<Map<MessageType, Long>>> getMessageTypeStats(
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            Map<MessageType, Long> stats = messageService.getMessageCountsByType(
                currentUser.getUserId().intValue());
            
            logger.debug("获取消息类型统计成功: 用户={}", currentUser.getUserId());
            
            return ResponseEntity.ok(ApiResponse.success(stats, "获取统计信息成功"));
        } catch (Exception e) {
            logger.error("获取消息类型统计失败", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 按类型获取消息列表
     */
    @GetMapping("/type/{messageType}")
    public ResponseEntity<ApiResponse<Page<MessageRefactoredDto>>> getMessagesByType(
            @PathVariable MessageType messageType,
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
            Page<MessageRefactoredDto> messages = messageService.getMessagesByType(
                currentUser.getUserId().intValue(), messageType, pageable);
            
            logger.debug("按类型获取消息成功: 用户={}, 类型={}, 页码={}, 数量={}", 
                        currentUser.getUserId(), messageType, page, messages.getNumberOfElements());
            
            return ResponseEntity.ok(ApiResponse.success(messages, "获取消息成功"));
        } catch (Exception e) {
            logger.error("按类型获取消息失败: messageType={}", messageType, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 按状态获取消息列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<MessageRefactoredDto>>> getMessagesByStatus(
            @PathVariable MessageStatus status,
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
            Page<MessageRefactoredDto> messages = messageService.getMessagesByStatus(
                currentUser.getUserId().intValue(), status, pageable);
            
            logger.debug("按状态获取消息成功: 用户={}, 状态={}, 页码={}, 数量={}", 
                        currentUser.getUserId(), status, page, messages.getNumberOfElements());
            
            return ResponseEntity.ok(ApiResponse.success(messages, "获取消息成功"));
        } catch (Exception e) {
            logger.error("按状态获取消息失败: status={}", status, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取消息失败: " + e.getMessage()));
        }
    }
    
    // ===== 工具方法 =====
    
    /**
     * 从请求中获取当前用户
     */
    private User getCurrentUser(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                logger.debug("请求中未找到JWT Token");
                return null;
            }
            
            String username = jwtUtil.extractUsername(token);
            if (username == null || !jwtUtil.validateToken(token, username)) {
                logger.debug("JWT Token无效或已过期");
                return null;
            }
            
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                logger.warn("用户不存在: username={}", username);
                return null;
            }
            
            return userOpt.get();
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