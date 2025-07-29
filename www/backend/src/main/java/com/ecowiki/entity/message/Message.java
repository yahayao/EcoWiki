package com.ecowiki.entity.message;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * 消息实体类
 * 
 * 对应数据库中的messages表，用于存储用户之间的消息通知。
 * 支持消息发送、接收、状态管理等功能。
 */
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
