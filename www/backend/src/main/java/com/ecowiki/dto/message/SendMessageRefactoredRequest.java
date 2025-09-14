/**
 * 发送消息请求DTO
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.dto.message;

import java.time.LocalDateTime;
import java.util.Map;

import com.ecowiki.enums.message.MessageType;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SendMessageRefactoredRequest {
    
    @NotNull(message = "接收者用户ID不能为空")
    private Integer recipientUserId;
    
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 10000, message = "消息内容不能超过10000个字符")
    private String content;
    
    @Size(max = 255, message = "消息主题不能超过255个字符")
    private String subject;
    
    private MessageType messageType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
    
    private Map<String, Object> metadata;
    
    // Constructors
    public SendMessageRefactoredRequest() {}
    
    public SendMessageRefactoredRequest(Integer recipientUserId, String content) {
        this.recipientUserId = recipientUserId;
        this.content = content;
    }
    
    // Getters and Setters
    public Integer getRecipientUserId() {
        return recipientUserId;
    }
    
    public void setRecipientUserId(Integer recipientUserId) {
        this.recipientUserId = recipientUserId;
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
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    
    public LocalDateTime getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}