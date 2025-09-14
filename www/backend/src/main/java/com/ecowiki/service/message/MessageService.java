/**
 * 消息服务接口
 * 
 * 功能：
 * - 定义消息系统的核心业务接口
 * - 支持多种消息类型的处理
 * - 提供统一的消息操作抽象
 * - 便于扩展和单元测试
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.service.message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecowiki.dto.message.MessageRefactoredDto;
import com.ecowiki.enums.message.MessageType;
import com.ecowiki.enums.message.MessageStatus;

public interface MessageService {
    
    /**
     * 发送消息
     * @param senderUserId 发送者用户ID
     * @param recipientUserId 接收者用户ID
     * @param content 消息内容
     * @param messageType 消息类型
     * @return 消息DTO
     */
    MessageRefactoredDto sendMessage(Integer senderUserId, Integer recipientUserId, 
                                   String content, MessageType messageType);
    
    /**
     * 发送带主题的消息
     * @param senderUserId 发送者用户ID
     * @param recipientUserId 接收者用户ID
     * @param content 消息内容
     * @param subject 消息主题
     * @param messageType 消息类型
     * @return 消息DTO
     */
    MessageRefactoredDto sendMessage(Integer senderUserId, Integer recipientUserId, 
                                   String content, String subject, MessageType messageType);
    
    /**
     * 发送带元数据的消息
     * @param senderUserId 发送者用户ID
     * @param recipientUserId 接收者用户ID
     * @param content 消息内容
     * @param subject 消息主题
     * @param messageType 消息类型
     * @param metadata 元数据
     * @param expireTime 过期时间
     * @return 消息DTO
     */
    MessageRefactoredDto sendMessage(Integer senderUserId, Integer recipientUserId, 
                                   String content, String subject, MessageType messageType,
                                   Map<String, Object> metadata, LocalDateTime expireTime);
    
    /**
     * 群发消息
     * @param senderUserId 发送者用户ID
     * @param recipientUserIds 接收者用户ID列表
     * @param content 消息内容
     * @param messageType 消息类型
     * @return 发送成功的消息DTO列表
     */
    List<MessageRefactoredDto> broadcastMessage(Integer senderUserId, List<Integer> recipientUserIds, 
                                              String content, MessageType messageType);
    
    /**
     * 发送系统通知
     * @param recipientUserId 接收者用户ID
     * @param content 消息内容
     * @param subject 消息主题
     * @return 消息DTO
     */
    MessageRefactoredDto sendSystemNotification(Integer recipientUserId, String content, String subject);
    
    /**
     * 批量发送系统通知
     * @param recipientUserIds 接收者用户ID列表
     * @param content 消息内容
     * @param subject 消息主题
     * @return 发送成功的消息DTO列表
     */
    List<MessageRefactoredDto> broadcastSystemNotification(List<Integer> recipientUserIds, 
                                                         String content, String subject);
    
    /**
     * 获取用户收到的消息（分页）
     * @param recipientUserId 接收者用户ID
     * @param pageable 分页参数
     * @return 消息DTO分页列表
     */
    Page<MessageRefactoredDto> getReceivedMessages(Integer recipientUserId, Pageable pageable);
    
    /**
     * 获取用户发送的消息（分页）
     * @param senderUserId 发送者用户ID
     * @param pageable 分页参数
     * @return 消息DTO分页列表
     */
    Page<MessageRefactoredDto> getSentMessages(Integer senderUserId, Pageable pageable);
    
    /**
     * 获取用户的所有消息（发送和接收）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 消息DTO分页列表
     */
    Page<MessageRefactoredDto> getUserMessages(Integer userId, Pageable pageable);
    
    /**
     * 获取两个用户之间的对话
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @param pageable 分页参数
     * @return 消息DTO分页列表
     */
    Page<MessageRefactoredDto> getConversation(Integer userId1, Integer userId2, Pageable pageable);
    
    /**
     * 按类型获取消息
     * @param userId 用户ID
     * @param messageType 消息类型
     * @param pageable 分页参数
     * @return 消息DTO分页列表
     */
    Page<MessageRefactoredDto> getMessagesByType(Integer userId, MessageType messageType, Pageable pageable);
    
    /**
     * 按状态获取消息
     * @param userId 用户ID
     * @param status 消息状态
     * @param pageable 分页参数
     * @return 消息DTO分页列表
     */
    Page<MessageRefactoredDto> getMessagesByStatus(Integer userId, MessageStatus status, Pageable pageable);
    
    /**
     * 获取用户未读消息数量
     * @param recipientUserId 接收者用户ID
     * @return 未读消息数量
     */
    Long getUnreadMessageCount(Integer recipientUserId);
    
    /**
     * 获取用户未读消息列表
     * @param recipientUserId 接收者用户ID
     * @return 未读消息DTO列表
     */
    List<MessageRefactoredDto> getUnreadMessages(Integer recipientUserId);
    
    /**
     * 按类型统计用户消息数量
     * @param userId 用户ID
     * @return 消息类型计数映射
     */
    Map<MessageType, Long> getMessageCountsByType(Integer userId);
    
    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @param userId 当前用户ID（权限验证）
     */
    void markAsRead(Integer messageId, Integer userId);
    
    /**
     * 标记用户所有未读消息为已读
     * @param recipientUserId 接收者用户ID
     */
    void markAllAsRead(Integer recipientUserId);
    
    /**
     * 批量标记消息为已读
     * @param messageIds 消息ID列表
     * @param userId 当前用户ID（权限验证）
     */
    void batchMarkAsRead(List<Integer> messageIds, Integer userId);
    
    /**
     * 删除消息（软删除）
     * @param messageId 消息ID
     * @param userId 当前用户ID（权限验证）
     */
    void deleteMessage(Integer messageId, Integer userId);
    
    /**
     * 批量删除消息
     * @param messageIds 消息ID列表
     * @param userId 当前用户ID（权限验证）
     */
    void batchDeleteMessages(List<Integer> messageIds, Integer userId);
    
    /**
     * 撤回消息（仅发送者可撤回，且有时间限制）
     * @param messageId 消息ID
     * @param userId 当前用户ID（权限验证）
     */
    void recallMessage(Integer messageId, Integer userId);
    
    /**
     * 清理过期消息
     * @return 清理的消息数量
     */
    int cleanupExpiredMessages();
    
    /**
     * 获取消息详情
     * @param messageId 消息ID
     * @param userId 当前用户ID（权限验证）
     * @return 消息DTO
     */
    MessageRefactoredDto getMessageDetail(Integer messageId, Integer userId);
    
    /**
     * 检查用户是否有权限访问消息
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasMessageAccess(Integer messageId, Integer userId);
}