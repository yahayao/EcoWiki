/**
 * 改进的消息实体类
 * 
 * 功能：
 * - 支持多种消息类型和状态
 * - 添加消息元数据和扩展属性
 * - 支持消息优先级和过期时间
 * - 提供更好的数据验证和转换
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.entity.message;

import java.time.LocalDateTime;

import com.ecowiki.enums.message.MessageStatus;
import com.ecowiki.enums.message.MessageType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "messages")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MessageRefactored {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;
    
    @Column(name = "recipient_user_id", nullable = false)
    @NotNull(message = "接收者用户ID不能为空")
    private Integer recipientUserId;
    
    @Column(name = "sender_user_id", nullable = false) 
    @NotNull(message = "发送者用户ID不能为空")
    private Integer senderUserId;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 10000, message = "消息内容不能超过10000个字符")
    private String content;
    
    @Column(name = "send_time")
    private LocalDateTime sendTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MessageStatus status = MessageStatus.UNREAD;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType = MessageType.USER_PRIVATE;
    
    @Column(name = "priority")
    private Integer priority;
    
    @Column(name = "expire_time")
    private LocalDateTime expireTime;
    
    @Column(name = "read_time")
    private LocalDateTime readTime;
    
    @Column(name = "subject", length = 255)
    @Size(max = 255, message = "消息主题不能超过255个字符")
    private String subject;
    
    /**
     * 消息元数据，存储JSON格式的扩展信息
     * 例如：链接、按钮、图片等
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 构造函数
    public MessageRefactored() {
        this.sendTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public MessageRefactored(Integer recipientUserId, Integer senderUserId, String content) {
        this();
        this.recipientUserId = recipientUserId;
        this.senderUserId = senderUserId;
        this.content = content;
    }
    
    public MessageRefactored(Integer recipientUserId, Integer senderUserId, String content, MessageType messageType) {
        this(recipientUserId, senderUserId, content);
        this.messageType = messageType;
        this.priority = messageType.getPriority().getLevel();
    }
    
    // JPA生命周期回调方法
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (sendTime == null) {
            sendTime = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = MessageStatus.UNREAD;
        }
        if (messageType == null) {
            messageType = MessageType.USER_PRIVATE;
        }
        if (priority == null && messageType != null) {
            priority = messageType.getPriority().getLevel();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 业务方法
    
    /**
     * 标记消息为已读
     */
    public void markAsRead() {
        if (this.status.canTransitionTo(MessageStatus.VIEWED)) {
            this.status = MessageStatus.VIEWED;
            this.readTime = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("无法将消息从 " + this.status + " 状态转换为已读状态");
        }
    }
    
    /**
     * 软删除消息
     */
    public void softDelete() {
        if (this.status.canTransitionTo(MessageStatus.DELETED)) {
            this.status = MessageStatus.DELETED;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("无法删除状态为 " + this.status + " 的消息");
        }
    }
    
    /**
     * 撤回消息（仅限发送者且在一定时间内）
     */
    public void recall() {
        LocalDateTime now = LocalDateTime.now();
        // 只有在发送后5分钟内才能撤回
        if (sendTime.plusMinutes(5).isAfter(now)) {
            this.status = MessageStatus.RECALLED;
            this.updatedAt = now;
        } else {
            throw new IllegalStateException("消息发送时间超过5分钟，无法撤回");
        }
    }
    
    /**
     * 检查消息是否已过期
     */
    public boolean isExpired() {
        return expireTime != null && LocalDateTime.now().isAfter(expireTime);
    }
    
    /**
     * 检查消息是否可见（未删除且未过期）
     */
    public boolean isVisible() {
        return status != MessageStatus.DELETED && !isExpired();
    }
    
    // Getters and Setters
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
    
    public Integer getSenderUserId() {
        return senderUserId;
    }
    
    public void setSenderUserId(Integer senderUserId) {
        this.senderUserId = senderUserId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
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
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
        if (messageType != null) {
            this.priority = messageType.getPriority().getLevel();
        }
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
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
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
}