/**
 * 消息服务实现类
 * 
 * 功能：
 * - 实现消息服务接口的所有方法
 * - 提供完整的消息管理功能
 * - 包含权限验证和业务逻辑
 * - 支持事务管理和异常处理
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.service.message.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.dto.message.MessageRefactoredDto;
import com.ecowiki.entity.message.MessageRefactored;
import com.ecowiki.entity.user.User;
import com.ecowiki.enums.message.MessageStatus;
import com.ecowiki.enums.message.MessageType;
import com.ecowiki.exception.message.MessageAccessDeniedException;
import com.ecowiki.exception.message.MessageNotFoundException;
import com.ecowiki.exception.message.UserNotFoundException;
import com.ecowiki.repository.message.MessageRefactoredRepository;
import com.ecowiki.repository.user.UserRepository;
import com.ecowiki.service.message.MessageService;
import com.ecowiki.util.message.MessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    
    @Autowired
    private MessageRefactoredRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageConverter messageConverter;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public MessageRefactoredDto sendMessage(Integer senderUserId, Integer recipientUserId, 
                                          String content, MessageType messageType) {
        return sendMessage(senderUserId, recipientUserId, content, null, messageType, null, null);
    }
    
    @Override
    public MessageRefactoredDto sendMessage(Integer senderUserId, Integer recipientUserId, 
                                          String content, String subject, MessageType messageType) {
        return sendMessage(senderUserId, recipientUserId, content, subject, messageType, null, null);
    }
    
    @Override
    public MessageRefactoredDto sendMessage(Integer senderUserId, Integer recipientUserId, 
                                          String content, String subject, MessageType messageType,
                                          Map<String, Object> metadata, LocalDateTime expireTime) {
        logger.info("发送消息: 发送者={}, 接收者={}, 类型={}", senderUserId, recipientUserId, messageType);
        
        // 验证用户是否存在
        validateUser(senderUserId, "发送者");
        validateUser(recipientUserId, "接收者");
        
        // 创建消息
        MessageRefactored message = new MessageRefactored(recipientUserId, senderUserId, content, messageType);
        message.setSubject(subject);
        message.setExpireTime(expireTime);
        
        // 处理元数据
        if (metadata != null && !metadata.isEmpty()) {
            try {
                String metadataJson = objectMapper.writeValueAsString(metadata);
                message.setMetadata(metadataJson);
            } catch (Exception e) {
                logger.warn("序列化消息元数据失败: {}", e.getMessage());
            }
        }
        
        // 保存消息
        MessageRefactored savedMessage = messageRepository.save(message);
        
        logger.info("消息发送成功: messageId={}", savedMessage.getMessageId());
        return messageConverter.convertToDto(savedMessage);
    }
    
    @Override
    public List<MessageRefactoredDto> broadcastMessage(Integer senderUserId, List<Integer> recipientUserIds, 
                                                     String content, MessageType messageType) {
        logger.info("群发消息: 发送者={}, 接收者数量={}, 类型={}", senderUserId, recipientUserIds.size(), messageType);
        
        // 验证发送者
        validateUser(senderUserId, "发送者");
        
        List<MessageRefactoredDto> sentMessages = new ArrayList<>();
        List<Integer> failedRecipients = new ArrayList<>();
        
        for (Integer recipientUserId : recipientUserIds) {
            try {
                // 验证接收者
                validateUser(recipientUserId, "接收者");
                
                // 发送消息
                MessageRefactoredDto message = sendMessage(senderUserId, recipientUserId, content, messageType);
                sentMessages.add(message);
                
            } catch (Exception e) {
                logger.warn("发送消息给用户 {} 失败: {}", recipientUserId, e.getMessage());
                failedRecipients.add(recipientUserId);
            }
        }
        
        if (sentMessages.isEmpty()) {
            throw new RuntimeException("没有成功发送任何消息");
        }
        
        if (!failedRecipients.isEmpty()) {
            logger.warn("部分消息发送失败，失败的接收者ID: {}", failedRecipients);
        }
        
        logger.info("群发消息完成: 成功={}, 失败={}", sentMessages.size(), failedRecipients.size());
        return sentMessages;
    }
    
    @Override
    public MessageRefactoredDto sendSystemNotification(Integer recipientUserId, String content, String subject) {
        logger.info("发送系统通知: 接收者={}, 主题={}", recipientUserId, subject);
        return sendMessage(0, recipientUserId, content, subject, MessageType.SYSTEM_NOTIFICATION);
    }
    
    @Override
    public List<MessageRefactoredDto> broadcastSystemNotification(List<Integer> recipientUserIds, 
                                                                String content, String subject) {
        logger.info("群发系统通知: 接收者数量={}, 主题={}", recipientUserIds.size(), subject);
        return broadcastMessage(0, recipientUserIds, content, MessageType.SYSTEM_NOTIFICATION);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MessageRefactoredDto> getReceivedMessages(Integer recipientUserId, Pageable pageable) {
        Page<MessageRefactored> messagePage = messageRepository.findVisibleByRecipientUserId(recipientUserId, pageable);
        return messageConverter.convertToDtoPage(messagePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MessageRefactoredDto> getSentMessages(Integer senderUserId, Pageable pageable) {
        Page<MessageRefactored> messagePage = messageRepository.findVisibleBySenderUserId(senderUserId, pageable);
        return messageConverter.convertToDtoPage(messagePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MessageRefactoredDto> getUserMessages(Integer userId, Pageable pageable) {
        Page<MessageRefactored> messagePage = messageRepository.findVisibleUserMessages(userId, pageable);
        return messageConverter.convertToDtoPage(messagePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MessageRefactoredDto> getConversation(Integer userId1, Integer userId2, Pageable pageable) {
        Page<MessageRefactored> messagePage = messageRepository.findVisibleConversation(userId1, userId2, pageable);
        return messageConverter.convertToDtoPage(messagePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MessageRefactoredDto> getMessagesByType(Integer userId, MessageType messageType, Pageable pageable) {
        Page<MessageRefactored> messagePage = messageRepository.findVisibleByUserIdAndType(userId, messageType, pageable);
        return messageConverter.convertToDtoPage(messagePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MessageRefactoredDto> getMessagesByStatus(Integer userId, MessageStatus status, Pageable pageable) {
        Page<MessageRefactored> messagePage = messageRepository.findVisibleByRecipientUserIdAndStatus(userId, status, pageable);
        return messageConverter.convertToDtoPage(messagePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getUnreadMessageCount(Integer recipientUserId) {
        return messageRepository.countUnreadVisibleMessages(recipientUserId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MessageRefactoredDto> getUnreadMessages(Integer recipientUserId) {
        List<MessageRefactored> messages = messageRepository.findUnreadVisibleMessages(recipientUserId);
        return messageConverter.convertToDtoList(messages);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<MessageType, Long> getMessageCountsByType(Integer userId) {
        Map<MessageType, Long> counts = new HashMap<>();
        for (MessageType type : MessageType.values()) {
            Long count = messageRepository.countVisibleByUserIdAndType(userId, type);
            counts.put(type, count);
        }
        return counts;
    }
    
    @Override
    public void markAsRead(Integer messageId, Integer userId) {
        logger.info("标记消息已读: messageId={}, userId={}", messageId, userId);
        
        MessageRefactored message = getMessageWithAccessCheck(messageId, userId);
        
        if (!message.getRecipientUserId().equals(userId)) {
            throw new MessageAccessDeniedException("只有消息接收者可以标记消息为已读");
        }
        
        try {
            message.markAsRead();
            messageRepository.save(message);
            logger.info("消息标记已读成功: messageId={}", messageId);
        } catch (IllegalStateException e) {
            logger.warn("标记消息已读失败: {}", e.getMessage());
            throw new RuntimeException("无法标记该消息为已读: " + e.getMessage());
        }
    }
    
    @Override
    public void markAllAsRead(Integer recipientUserId) {
        logger.info("标记用户所有消息为已读: userId={}", recipientUserId);
        
        int updatedCount = messageRepository.markAllUnreadAsRead(recipientUserId);
        
        logger.info("批量标记已读完成: userId={}, 更新数量={}", recipientUserId, updatedCount);
    }
    
    @Override
    public void batchMarkAsRead(List<Integer> messageIds, Integer userId) {
        logger.info("批量标记消息已读: messageIds={}, userId={}", messageIds, userId);
        
        for (Integer messageId : messageIds) {
            try {
                markAsRead(messageId, userId);
            } catch (Exception e) {
                logger.warn("标记消息 {} 已读失败: {}", messageId, e.getMessage());
            }
        }
    }
    
    @Override
    public void deleteMessage(Integer messageId, Integer userId) {
        logger.info("删除消息: messageId={}, userId={}", messageId, userId);
        
        MessageRefactored message = getMessageWithAccessCheck(messageId, userId);
        
        try {
            message.softDelete();
            messageRepository.save(message);
            logger.info("消息删除成功: messageId={}", messageId);
        } catch (IllegalStateException e) {
            logger.warn("删除消息失败: {}", e.getMessage());
            throw new RuntimeException("无法删除该消息: " + e.getMessage());
        }
    }
    
    @Override
    public void batchDeleteMessages(List<Integer> messageIds, Integer userId) {
        logger.info("批量删除消息: messageIds={}, userId={}", messageIds, userId);
        
        for (Integer messageId : messageIds) {
            try {
                deleteMessage(messageId, userId);
            } catch (Exception e) {
                logger.warn("删除消息 {} 失败: {}", messageId, e.getMessage());
            }
        }
    }
    
    @Override
    public void recallMessage(Integer messageId, Integer userId) {
        logger.info("撤回消息: messageId={}, userId={}", messageId, userId);
        
        MessageRefactored message = getMessageWithAccessCheck(messageId, userId);
        
        if (!message.getSenderUserId().equals(userId)) {
            throw new MessageAccessDeniedException("只有消息发送者可以撤回消息");
        }
        
        try {
            message.recall();
            messageRepository.save(message);
            logger.info("消息撤回成功: messageId={}", messageId);
        } catch (IllegalStateException e) {
            logger.warn("撤回消息失败: {}", e.getMessage());
            throw new RuntimeException("无法撤回该消息: " + e.getMessage());
        }
    }
    
    @Override
    public int cleanupExpiredMessages() {
        logger.info("开始清理过期消息");
        
        List<MessageRefactored> expiredMessages = messageRepository.findExpiredMessages();
        int count = 0;
        
        for (MessageRefactored message : expiredMessages) {
            if (message.isExpired()) {
                message.softDelete();
                messageRepository.save(message);
                count++;
            }
        }
        
        logger.info("过期消息清理完成: 清理数量={}", count);
        return count;
    }
    
    @Override
    @Transactional(readOnly = true)
    public MessageRefactoredDto getMessageDetail(Integer messageId, Integer userId) {
        MessageRefactored message = getMessageWithAccessCheck(messageId, userId);
        return messageConverter.convertToDto(message);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasMessageAccess(Integer messageId, Integer userId) {
        try {
            getMessageWithAccessCheck(messageId, userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证用户是否存在
     */
    private void validateUser(Integer userId, String userType) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(userType + "用户ID无效");
        }
        
        // 系统消息的发送者ID为0，无需验证
        if (userId == 0) {
            return;
        }
        
        Optional<User> user = userRepository.findByUserId(userId.longValue());
        if (user.isEmpty()) {
            throw new UserNotFoundException(userType + "用户不存在: " + userId);
        }
        
        if (!user.get().getActive()) {
            throw new UserNotFoundException(userType + "用户已被禁用: " + userId);
        }
    }
    
    /**
     * 获取消息并检查访问权限
     */
    private MessageRefactored getMessageWithAccessCheck(Integer messageId, Integer userId) {
        Optional<MessageRefactored> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            throw new MessageNotFoundException("消息不存在: " + messageId);
        }
        
        MessageRefactored message = messageOpt.get();
        
        // 检查访问权限：只有发送者或接收者才能访问消息
        if (!message.getSenderUserId().equals(userId) && !message.getRecipientUserId().equals(userId)) {
            throw new MessageAccessDeniedException("无权限访问该消息");
        }
        
        return message;
    }
}