/**
 * 消息传输对象DTO类
 * 
 * 功能：
 * - 封装消息的完整信息用于前后端数据传输
 * - 包含发送者和接收者的用户信息
 * - 支持消息状态管理（已读/未读）
 * - 提供消息时间戳和内容记录功能
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.dto.message;

import java.time.LocalDateTime;
public class MessageDto {
    
    /** 消息唯一标识ID */
    private Integer messageId;
    
    /** 接收者用户ID */
    private Integer recipientUserId;
    
    /** 接收者用户名 */
    private String recipientUsername;
    
    /** 发送者用户ID */
    private Integer senderUserId;
    
    /** 发送者用户名 */
    private String senderUsername;
    
    /** 消息内容 */
    private String content;
    
    /** 消息发送时间 */
    private LocalDateTime sendTime;
    
    /** 消息状态（如：UNREAD/READ） */
    private String status;
    
    /**
     * 默认构造函数
     */
    public MessageDto() {}
    
    /**
     * 全参数构造函数
     * @param messageId 消息唯一标识ID
     * @param recipientUserId 接收者用户ID
     * @param recipientUsername 接收者用户名
     * @param senderUserId 发送者用户ID
     * @param senderUsername 发送者用户名
     * @param content 消息内容
     * @param sendTime 消息发送时间
     * @param status 消息状态
     */
    public MessageDto(Integer messageId, Integer recipientUserId, String recipientUsername,
                     Integer senderUserId, String senderUsername, String content,
                     LocalDateTime sendTime, String status) {
        this.messageId = messageId;
        this.recipientUserId = recipientUserId;
        this.recipientUsername = recipientUsername;
        this.senderUserId = senderUserId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.sendTime = sendTime;
        this.status = status;
    }

    // Getter 和 Setter 方法
    
    /**
     * 获取消息ID
     * @return 消息唯一标识ID
     */
    public Integer getMessageId() {
        return messageId;
    }
    
    /**
     * 设置消息ID
     * @param messageId 消息唯一标识ID
     */
    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }
    
    /**
     * 获取接收者用户ID
     * @return 接收者用户ID
     */
    public Integer getRecipientUserId() {
        return recipientUserId;
    }
    
    /**
     * 设置接收者用户ID
     * @param recipientUserId 接收者用户ID
     */
    public void setRecipientUserId(Integer recipientUserId) {
        this.recipientUserId = recipientUserId;
    }
    
    /**
     * 获取接收者用户名
     * @return 接收者用户名
     */
    public String getRecipientUsername() {
        return recipientUsername;
    }
    
    /**
     * 设置接收者用户名
     * @param recipientUsername 接收者用户名
     */
    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }
    
    /**
     * 获取发送者用户ID
     * @return 发送者用户ID
     */
    public Integer getSenderUserId() {
        return senderUserId;
    }
    
    /**
     * 设置发送者用户ID
     * @param senderUserId 发送者用户ID
     */
    public void setSenderUserId(Integer senderUserId) {
        this.senderUserId = senderUserId;
    }
    
    /**
     * 获取发送者用户名
     * @return 发送者用户名
     */
    public String getSenderUsername() {
        return senderUsername;
    }
    
    /**
     * 设置发送者用户名
     * @param senderUsername 发送者用户名
     */
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
    
    /**
     * 获取消息内容
     * @return 消息内容
     */
    public String getContent() {
        return content;
    }
    
    /**
     * 设置消息内容
     * @param content 消息内容
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * 获取消息发送时间
     * @return 消息发送时间
     */
    public LocalDateTime getSendTime() {
        return sendTime;
    }
    
    /**
     * 设置消息发送时间
     * @param sendTime 消息发送时间
     */
    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }
    
    /**
     * 获取消息状态
     * @return 消息状态（如：UNREAD/READ）
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * 设置消息状态
     * @param status 消息状态（如：UNREAD/read）
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
