/**
 * 改进的消息传输对象DTO类
 * 
 * 功能：
 * - 支持多种消息类型和状态的传输
 * - 包含完整的用户信息和元数据
 * - 提供更好的JSON序列化支持
 * - 支持消息优先级和过期时间
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.dto.message;

import java.time.LocalDateTime;
import java.util.Map;

import com.ecowiki.enums.message.MessageStatus;
import com.ecowiki.enums.message.MessageType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageRefactoredDto {
    
    /** 消息唯一标识ID */
    private Integer messageId;
    
    /** 接收者用户ID */
    private Integer recipientUserId;
    
    /** 接收者用户名 */
    private String recipientUsername;
    
    /** 接收者真实姓名 */
    private String recipientFullName;
    
    /** 接收者头像URL */
    private String recipientAvatarUrl;
    
    /** 发送者用户ID */
    private Integer senderUserId;
    
    /** 发送者用户名 */
    private String senderUsername;
    
    /** 发送者真实姓名 */
    private String senderFullName;
    
    /** 发送者头像URL */
    private String senderAvatarUrl;
    
    /** 消息内容 */
    private String content;
    
    /** 消息主题 */
    private String subject;
    
    /** 消息发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
    
    /** 消息状态 */
    private MessageStatus status;
    
    /** 消息状态描述 */
    private String statusDescription;
    
    /** 消息类型 */
    private MessageType messageType;
    
    /** 消息类型描述 */
    private String messageTypeDescription;
    
    /** 消息优先级 */
    private Integer priority;
    
    /** 消息过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
    
    /** 已读时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /** 消息元数据 */
    private Map<String, Object> metadata;
    
    /** 是否已过期 */
    private Boolean expired;
    
    /** 是否可见 */
    private Boolean visible;
    
    /** 是否为系统消息 */
    private Boolean systemGenerated;
    
    /**
     * 默认构造函数
     */
    public MessageRefactoredDto() {}
    
    /**
     * 基本构造函数
     */
    public MessageRefactoredDto(Integer messageId, Integer recipientUserId, String recipientUsername,
                               Integer senderUserId, String senderUsername, String content,
                               LocalDateTime sendTime, MessageStatus status, MessageType messageType) {
        this.messageId = messageId;
        this.recipientUserId = recipientUserId;
        this.recipientUsername = recipientUsername;
        this.senderUserId = senderUserId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.sendTime = sendTime;
        this.status = status;
        this.messageType = messageType;
        this.statusDescription = status != null ? status.getChineseDescription() : null;
        this.messageTypeDescription = messageType != null ? messageType.getDescription() : null;
        this.priority = messageType != null ? messageType.getPriority().getLevel() : null;
        this.systemGenerated = messageType != null ? messageType.isSystemGenerated() : false;
    }
    
    // Getter 和 Setter 方法
    
    public Integer getMessageId() {
        return messageId;
    }
    
    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }
    
    public Integer getRecipientUserId() {
        return recipientUserId;
    }
    
    public void setRecipientUserId(Integer recipientUserId) {
        this.recipientUserId = recipientUserId;
    }
    
    public String getRecipientUsername() {
        return recipientUsername;
    }
    
    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }
    
    public String getRecipientFullName() {
        return recipientFullName;
    }
    
    public void setRecipientFullName(String recipientFullName) {
        this.recipientFullName = recipientFullName;
    }
    
    public String getRecipientAvatarUrl() {
        return recipientAvatarUrl;
    }
    
    public void setRecipientAvatarUrl(String recipientAvatarUrl) {
        this.recipientAvatarUrl = recipientAvatarUrl;
    }
    
    public Integer getSenderUserId() {
        return senderUserId;
    }
    
    public void setSenderUserId(Integer senderUserId) {
        this.senderUserId = senderUserId;
    }
    
    public String getSenderUsername() {
        return senderUsername;
    }
    
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
    
    public String getSenderFullName() {
        return senderFullName;
    }
    
    public void setSenderFullName(String senderFullName) {
        this.senderFullName = senderFullName;
    }
    
    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
    }
    
    public void setSenderAvatarUrl(String senderAvatarUrl) {
        this.senderAvatarUrl = senderAvatarUrl;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public LocalDateTime getSendTime() {
        return sendTime;
    }
    
    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }
    
    public MessageStatus getStatus() {
        return status;
    }
    
    public void setStatus(MessageStatus status) {
        this.status = status;
        this.statusDescription = status != null ? status.getChineseDescription() : null;
    }
    
    public String getStatusDescription() {
        return statusDescription;
    }
    
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
        this.messageTypeDescription = messageType != null ? messageType.getDescription() : null;
        this.priority = messageType != null ? messageType.getPriority().getLevel() : null;
        this.systemGenerated = messageType != null ? messageType.isSystemGenerated() : false;
    }
    
    public String getMessageTypeDescription() {
        return messageTypeDescription;
    }
    
    public void setMessageTypeDescription(String messageTypeDescription) {
        this.messageTypeDescription = messageTypeDescription;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public LocalDateTime getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
    
    public LocalDateTime getReadTime() {
        return readTime;
    }
    
    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Boolean getExpired() {
        return expired;
    }
    
    public void setExpired(Boolean expired) {
        this.expired = expired;
    }
    
    public Boolean getVisible() {
        return visible;
    }
    
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
    
    public Boolean getSystemGenerated() {
        return systemGenerated;
    }
    
    public void setSystemGenerated(Boolean systemGenerated) {
        this.systemGenerated = systemGenerated;
    }
}