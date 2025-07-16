package com.ecowiki.dto;

import java.time.LocalDateTime;

/**
 * 消息传输对象
 * 
 * 用于前后端传输消息数据，包含发送者和接收者的用户信息
 */
public class MessageDto {
    
    private Integer messageId;
    private Integer recipientUserId;
    private String recipientUsername;
    private Integer senderUserId;
    private String senderUsername;
    private String content;
    private LocalDateTime sendTime;
    private String status;
    
    // 构造函数
    public MessageDto() {}
    
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
    
    public String getRecipientUsername() {
        return recipientUsername;
    }
    
    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
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
}


