/**
 * 群发消息请求DTO
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.dto.message;

import java.util.List;

import com.ecowiki.enums.message.MessageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class BroadcastMessageRefactoredRequest {
    
    @NotEmpty(message = "接收者用户ID列表不能为空")
    private List<Integer> recipientUserIds;
    
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 10000, message = "消息内容不能超过10000个字符")
    private String content;
    
    @Size(max = 255, message = "消息主题不能超过255个字符")
    private String subject;
    
    private MessageType messageType;
    
    // Constructors
    public BroadcastMessageRefactoredRequest() {}
    
    public BroadcastMessageRefactoredRequest(List<Integer> recipientUserIds, String content) {
        this.recipientUserIds = recipientUserIds;
        this.content = content;
    }
    
    // Getters and Setters
    public List<Integer> getRecipientUserIds() {
        return recipientUserIds;
    }
    
    public void setRecipientUserIds(List<Integer> recipientUserIds) {
        this.recipientUserIds = recipientUserIds;
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
}