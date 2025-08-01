package com.ecowiki.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.dto.message.MessageDto;
import com.ecowiki.entity.message.Message;
import com.ecowiki.entity.user.User;
import com.ecowiki.repository.message.MessageRepository;
import com.ecowiki.repository.user.UserRepository;

/**
 * 消息服务类
 * 
 * 提供消息的发送、接收、查询和管理功能
 */
@Service
@Transactional
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 发送消息
     * @param senderUserId 发送者用户ID
     * @param recipientUserId 接收者用户ID
     * @param content 消息内容
     * @return 消息DTO
     */
    public MessageDto sendMessage(Integer senderUserId, Integer recipientUserId, String content) {
        // 验证用户是否存在
        Optional<User> sender = userRepository.findByUserId(senderUserId.longValue());
        Optional<User> recipient = userRepository.findByUserId(recipientUserId.longValue());
        
        if (sender.isEmpty()) {
            throw new RuntimeException("发送者用户不存在");
        }
        if (recipient.isEmpty()) {
            throw new RuntimeException("接收者用户不存在");
        }
        
        // 创建消息
        Message message = new Message(recipientUserId, senderUserId, content);
        message.setSendTime(LocalDateTime.now());
        message.setStatus("未读");
        
        Message savedMessage = messageRepository.save(message);
        
        // 转换为DTO
        return convertToDto(savedMessage, sender.get(), recipient.get());
    }
    
    /**
     * 获取用户收到的消息（分页）
     * @param recipientUserId 接收者用户ID
     * @param pageable 分页参数
     * @return 消息DTO分页列表
     */
    public Page<MessageDto> getReceivedMessages(Integer recipientUserId, Pageable pageable) {
        Page<Message> messagePage = messageRepository.findByRecipientUserId(recipientUserId, pageable);
        return convertToDtoPage(messagePage);
    }
    
    /**
     * 获取用户发送的消息（分页）
     * @param senderUserId 发送者用户ID
     * @param pageable 分页参数
     * @return 消息DTO分页列表
     */
    public Page<MessageDto> getSentMessages(Integer senderUserId, Pageable pageable) {
        Page<Message> messagePage = messageRepository.findBySenderUserId(senderUserId, pageable);
        return convertToDtoPage(messagePage);
    }
    
    /**
     * 获取用户的所有消息（发送和接收）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 消息DTO分页列表
     */
    public Page<MessageDto> getUserMessages(Integer userId, Pageable pageable) {
        Page<Message> messagePage = messageRepository.findUserMessages(userId, pageable);
        return convertToDtoPage(messagePage);
    }
    
    /**
     * 获取两个用户之间的对话
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @param pageable 分页参数
     * @return 消息DTO分页列表
     */
    public Page<MessageDto> getConversation(Integer userId1, Integer userId2, Pageable pageable) {
        Page<Message> messagePage = messageRepository.findConversation(userId1, userId2, pageable);
        return convertToDtoPage(messagePage);
    }
    
    /**
     * 获取用户未读消息数量
     * @param recipientUserId 接收者用户ID
     * @return 未读消息数量
     */
    public Long getUnreadMessageCount(Integer recipientUserId) {
        return messageRepository.countUnreadMessages(recipientUserId);
    }
    
    /**
     * 获取用户未读消息列表
     * @param recipientUserId 接收者用户ID
     * @return 未读消息DTO列表
     */
    public List<MessageDto> getUnreadMessages(Integer recipientUserId) {
        List<Message> messages = messageRepository.findUnreadMessages(recipientUserId);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 标记消息为已读
     * @param messageId 消息ID
     */
    public void markAsRead(Integer messageId) {
        messageRepository.markAsRead(messageId);
    }
    
    /**
     * 标记用户所有未读消息为已读
     * @param recipientUserId 接收者用户ID
     */
    public void markAllAsRead(Integer recipientUserId) {
        messageRepository.markAllAsRead(recipientUserId);
    }
    
    /**
     * 删除消息
     * @param messageId 消息ID
     * @param userId 当前用户ID（只能删除自己发送或接收的消息）
     */
    public void deleteMessage(Integer messageId, Integer userId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            throw new RuntimeException("消息不存在");
        }
        
        Message message = messageOpt.get();
        if (!message.getSenderUserId().equals(userId) && !message.getRecipientUserId().equals(userId)) {
            throw new RuntimeException("无权限删除该消息");
        }
        
        messageRepository.deleteById(messageId);
    }
    
    /**
     * 群发消息
     * @param senderUserId 发送者用户ID
     * @param recipientUserIds 接收者用户ID列表
     * @param content 消息内容
     * @return 消息DTO列表
     */
    public List<MessageDto> broadcastMessage(Integer senderUserId, List<Integer> recipientUserIds, String content) {
        // 验证发送者是否存在
        Optional<User> sender = userRepository.findByUserId(senderUserId.longValue());
        if (sender.isEmpty()) {
            throw new RuntimeException("发送者用户不存在");
        }
        
        List<MessageDto> sentMessages = new ArrayList<>();
        
        for (Integer recipientUserId : recipientUserIds) {
            try {
                // 验证接收者是否存在
                Optional<User> recipient = userRepository.findByUserId(recipientUserId.longValue());
                if (recipient.isEmpty()) {
                    System.err.println("接收者用户不存在: " + recipientUserId);
                    continue; // 跳过不存在的用户，继续发送给其他用户
                }
                
                // 创建消息
                Message message = new Message(recipientUserId, senderUserId, content);
                message.setSendTime(LocalDateTime.now());
                message.setStatus("未读");
                
                Message savedMessage = messageRepository.save(message);
                
                // 转换为DTO并添加到结果列表
                MessageDto messageDto = convertToDto(savedMessage, sender.get(), recipient.get());
                sentMessages.add(messageDto);
                
            } catch (Exception e) {
                System.err.println("发送消息给用户 " + recipientUserId + " 失败: " + e.getMessage());
                // 继续发送给其他用户
            }
        }
        
        if (sentMessages.isEmpty()) {
            throw new RuntimeException("没有成功发送任何消息，请检查接收者用户ID");
        }
        
        return sentMessages;
    }

    /**
     * 转换Message实体为MessageDto
     * @param message 消息实体
     * @return 消息DTO
     */
    private MessageDto convertToDto(Message message) {
        // 获取发送者和接收者用户信息
        Optional<User> sender = userRepository.findByUserId(message.getSenderUserId().longValue());
        Optional<User> recipient = userRepository.findByUserId(message.getRecipientUserId().longValue());
        
        return convertToDto(message, sender.orElse(null), recipient.orElse(null));
    }
    
    /**
     * 转换Message实体为MessageDto（包含用户信息）
     * @param message 消息实体
     * @param sender 发送者用户
     * @param recipient 接收者用户
     * @return 消息DTO
     */
    private MessageDto convertToDto(Message message, User sender, User recipient) {
        MessageDto dto = new MessageDto();
        dto.setMessageId(message.getMessageId());
        dto.setRecipientUserId(message.getRecipientUserId());
        dto.setSenderUserId(message.getSenderUserId());
        dto.setContent(message.getContent());
        dto.setSendTime(message.getSendTime());
        dto.setStatus(message.getStatus());
        
        if (sender != null) {
            dto.setSenderUsername(sender.getUsername());
        }
        if (recipient != null) {
            dto.setRecipientUsername(recipient.getUsername());
        }
        
        return dto;
    }
    
    /**
     * 转换Message分页为MessageDto分页
     * @param messagePage 消息实体分页
     * @return 消息DTO分页
     */
    private Page<MessageDto> convertToDtoPage(Page<Message> messagePage) {
        List<MessageDto> dtos = messagePage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, messagePage.getPageable(), messagePage.getTotalElements());
    }
}
