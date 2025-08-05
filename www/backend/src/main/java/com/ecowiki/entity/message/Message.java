/**
 * 消息实体类
 * 
 * 功能：
 * - 定义用户消息的数据模型
 * - 包含发送者、接收者和消息内容
 * - 支持消息状态管理和时间戳记录
 * - 提供消息发送和接收的数据存储
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.entity.message;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "messages")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;
    
    @Column(name = "recipient_user_id", nullable = false)
    private Integer recipientUserId;
    
    @Column(name = "sender_user_id", nullable = false) 
    private Integer senderUserId;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "send_time")
    private LocalDateTime sendTime;
    
    @Column(name = "status", length = 50)
    private String status = "未读";
    
    // 构造函数
    public Message() {
        this.sendTime = LocalDateTime.now();
    }
    
    public Message(Integer recipientUserId, Integer senderUserId, String content) {
        this();
        this.recipientUserId = recipientUserId;
        this.senderUserId = senderUserId;
        this.content = content;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @PrePersist
    protected void onCreate() {
        if (sendTime == null) {
            sendTime = LocalDateTime.now();
        }
        if (status == null) {
            status = "未读";
        }
    }
}
