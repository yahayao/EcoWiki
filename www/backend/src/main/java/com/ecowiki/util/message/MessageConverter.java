/**
 * 消息转换器工具类
 * 
 * 功能：
 * - 提供Entity和DTO之间的转换
 * - 批量转换和分页转换支持
 * - 用户信息缓存优化
 * - 元数据处理和序列化
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.util.message;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import com.ecowiki.dto.message.MessageRefactoredDto;
import com.ecowiki.entity.message.MessageRefactored;
import com.ecowiki.entity.user.User;
import com.ecowiki.repository.user.UserRepository;
import com.ecowiki.enums.message.MessageStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MessageConverter {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 用户信息缓存，避免重复查询
    private final Map<Integer, User> userCache = new HashMap<>();
    
    /**
     * 将消息实体转换为DTO
     */
    public MessageRefactoredDto convertToDto(MessageRefactored message) {
        if (message == null) {
            return null;
        }
        
        MessageRefactoredDto dto = new MessageRefactoredDto();
        
        // 基本字段映射
        dto.setMessageId(message.getMessageId());
        dto.setRecipientUserId(message.getRecipientUserId());
        dto.setSenderUserId(message.getSenderUserId());
        dto.setContent(message.getContent());
        dto.setSubject(message.getSubject());
        dto.setSendTime(message.getSendTime());
        dto.setStatus(message.getStatus());
        dto.setMessageType(message.getMessageType());
        dto.setPriority(message.getPriority());
        dto.setExpireTime(message.getExpireTime());
        dto.setReadTime(message.getReadTime());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        
        // 设置状态和类型描述
        if (message.getStatus() != null) {
            dto.setStatusDescription(message.getStatus().getChineseDescription());
        }
        if (message.getMessageType() != null) {
            dto.setMessageTypeDescription(message.getMessageType().getDescription());
            dto.setSystemGenerated(message.getMessageType().isSystemGenerated());
        }
        
        // 设置计算字段
        dto.setExpired(message.isExpired());
        dto.setVisible(message.isVisible());
        
        // 获取用户信息
        User sender = getUserFromCache(message.getSenderUserId());
        User recipient = getUserFromCache(message.getRecipientUserId());
        
        if (sender != null) {
            dto.setSenderUsername(sender.getUsername());
            dto.setSenderFullName(sender.getFullName());
            dto.setSenderAvatarUrl(sender.getAvatarUrl());
        }
        
        if (recipient != null) {
            dto.setRecipientUsername(recipient.getUsername());
            dto.setRecipientFullName(recipient.getFullName());
            dto.setRecipientAvatarUrl(recipient.getAvatarUrl());
        }
        
        // 处理元数据
        if (message.getMetadata() != null) {
            try {
                Map<String, Object> metadata = objectMapper.readValue(
                    message.getMetadata(), 
                    new TypeReference<Map<String, Object>>() {}
                );
                dto.setMetadata(metadata);
            } catch (Exception e) {
                // 如果解析失败，记录日志但不影响主流程
                System.err.println("解析消息元数据失败: " + e.getMessage());
            }
        }
        
        return dto;
    }
    
    /**
     * 将DTO转换为消息实体
     */
    public MessageRefactored convertToEntity(MessageRefactoredDto dto) {
        if (dto == null) {
            return null;
        }
        
        MessageRefactored message = new MessageRefactored();
        
        message.setMessageId(dto.getMessageId());
        message.setRecipientUserId(dto.getRecipientUserId());
        message.setSenderUserId(dto.getSenderUserId());
        message.setContent(dto.getContent());
        message.setSubject(dto.getSubject());
        message.setSendTime(dto.getSendTime());
        message.setStatus(dto.getStatus());
        message.setMessageType(dto.getMessageType());
        message.setPriority(dto.getPriority());
        message.setExpireTime(dto.getExpireTime());
        message.setReadTime(dto.getReadTime());
        message.setCreatedAt(dto.getCreatedAt());
        message.setUpdatedAt(dto.getUpdatedAt());
        
        // 处理元数据
        if (dto.getMetadata() != null) {
            try {
                String metadata = objectMapper.writeValueAsString(dto.getMetadata());
                message.setMetadata(metadata);
            } catch (Exception e) {
                System.err.println("序列化消息元数据失败: " + e.getMessage());
            }
        }
        
        return message;
    }
    
    /**
     * 批量转换消息实体为DTO列表
     */
    public List<MessageRefactoredDto> convertToDtoList(List<MessageRefactored> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        
        // 预加载所有相关用户信息到缓存
        preloadUserCache(messages);
        
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换分页消息为DTO分页
     */
    public Page<MessageRefactoredDto> convertToDtoPage(Page<MessageRefactored> messagePage) {
        if (messagePage == null || messagePage.isEmpty()) {
            return Page.empty();
        }
        
        List<MessageRefactoredDto> dtoList = convertToDtoList(messagePage.getContent());
        return new PageImpl<>(dtoList, messagePage.getPageable(), messagePage.getTotalElements());
    }
    
    /**
     * 从缓存或数据库获取用户信息
     */
    private User getUserFromCache(Integer userId) {
        if (userId == null) {
            return null;
        }
        
        // 先检查缓存
        User user = userCache.get(userId);
        if (user != null) {
            return user;
        }
        
        // 缓存中没有则从数据库查询
        user = userRepository.findByUserId(userId.longValue()).orElse(null);
        if (user != null) {
            userCache.put(userId, user);
        }
        
        return user;
    }
    
    /**
     * 预加载用户信息到缓存
     */
    private void preloadUserCache(List<MessageRefactored> messages) {
        // 收集所有需要的用户ID
        List<Long> userIds = messages.stream()
                .flatMap(msg -> List.of(
                    msg.getSenderUserId().longValue(),
                    msg.getRecipientUserId().longValue()
                ).stream())
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userRepository.findAllById(userIds);
        
        // 更新缓存
        for (User user : users) {
            userCache.put(user.getUserId().intValue(), user);
        }
    }
    
    /**
     * 清空用户缓存
     */
    public void clearUserCache() {
        userCache.clear();
    }
    
    /**
     * 创建系统消息DTO
     */
    public MessageRefactoredDto createSystemMessageDto(Integer recipientUserId, String content, String subject) {
        MessageRefactoredDto dto = new MessageRefactoredDto();
        dto.setRecipientUserId(recipientUserId);
        dto.setSenderUserId(0); // 系统消息发送者ID为0
        dto.setContent(content);
        dto.setSubject(subject);
        dto.setSendTime(LocalDateTime.now());
        dto.setStatus(MessageStatus.UNREAD);
        dto.setMessageType(com.ecowiki.enums.message.MessageType.SYSTEM_NOTIFICATION);
        dto.setSystemGenerated(true);
        
        // 设置系统发送者信息
        dto.setSenderUsername("系统");
        dto.setSenderFullName("EcoWiki系统");
        
        return dto;
    }
}