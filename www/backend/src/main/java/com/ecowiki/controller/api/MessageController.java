package com.ecowiki.controller.api;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
import com.ecowiki.dto.MessageDto;
import com.ecowiki.dto.SendMessageRequest;
import com.ecowiki.entity.User;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.MessageService;
import com.ecowiki.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 消息API控制器
 * 
 * 提供消息相关的REST API接口
 */
@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:5173"}, allowCredentials = "true")
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 发送消息
     * @param request 发送消息请求
     * @param httpRequest HTTP请求
     * @return 发送结果
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(
            @RequestBody SendMessageRequest request,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            MessageDto message = messageService.sendMessage(
                currentUser.getUserId().intValue(),
                request.getRecipientUserId(),
                request.getContent()
            );
            
            return ResponseEntity.ok(ApiResponse.success(message, "消息发送成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("发送失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取收到的消息
     * @param page 页码
     * @param size 每页大小
     * @param httpRequest HTTP请求
     * @return 消息列表
     */
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<Page<MessageDto>>> getReceivedMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<MessageDto> messages = messageService.getReceivedMessages(
                currentUser.getUserId().intValue(), pageable);
            
            return ResponseEntity.ok(ApiResponse.success(messages, "获取消息成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取发送的消息
     * @param page 页码
     * @param size 每页大小
     * @param httpRequest HTTP请求
     * @return 消息列表
     */
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<Page<MessageDto>>> getSentMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<MessageDto> messages = messageService.getSentMessages(
                currentUser.getUserId().intValue(), pageable);
            
            return ResponseEntity.ok(ApiResponse.success(messages, "获取消息成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有消息（收发）
     * @param page 页码
     * @param size 每页大小
     * @param httpRequest HTTP请求
     * @return 消息列表
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<MessageDto>>> getAllMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<MessageDto> messages = messageService.getUserMessages(
                currentUser.getUserId().intValue(), pageable);
            
            return ResponseEntity.ok(ApiResponse.success(messages, "获取消息成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取与指定用户的对话
     * @param userId 对方用户ID
     * @param page 页码
     * @param size 每页大小
     * @param httpRequest HTTP请求
     * @return 对话消息列表
     */
    @GetMapping("/conversation/{userId}")
    public ResponseEntity<ApiResponse<Page<MessageDto>>> getConversation(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<MessageDto> messages = messageService.getConversation(
                currentUser.getUserId().intValue(), userId, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(messages, "获取对话成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取对话失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取未读消息数量
     * @param httpRequest HTTP请求
     * @return 未读消息数量
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
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取未读数量失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取未读消息列表
     * @param httpRequest HTTP请求
     * @return 未读消息列表
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getUnreadMessages(HttpServletRequest httpRequest) {
        try {
            User currentUser = getCurrentUser(httpRequest);
            if (currentUser == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("用户未登录"));
            }
            
            List<MessageDto> messages = messageService.getUnreadMessages(currentUser.getUserId().intValue());
            return ResponseEntity.ok(ApiResponse.success(messages, "获取未读消息成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取未读消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @param httpRequest HTTP请求
     * @return 操作结果
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
            
            messageService.markAsRead(messageId);
            return ResponseEntity.ok(ApiResponse.success(null, "标记已读成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("标记已读失败: " + e.getMessage()));
        }
    }
    
    /**
     * 标记所有消息为已读
     * @param httpRequest HTTP请求
     * @return 操作结果
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
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("标记已读失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除消息
     * @param messageId 消息ID
     * @param httpRequest HTTP请求
     * @return 操作结果
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
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除消息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 从请求中获取当前用户
     * @param request HTTP请求
     * @return 当前用户，如果未登录则返回null
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
            return null;
        }
    }
    
    /**
     * 从请求头提取JWT Token
     * @param request HTTP请求
     * @return Bearer Token字符串，若无则为null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
