package com.ecowiki.dto;

/**
 * 发送消息请求对象
 */
public class SendMessageRequest {
    private Integer recipientUserId;
    private String content;
    
    public SendMessageRequest() {}
    
    public SendMessageRequest(Integer recipientUserId, String content) {
        this.recipientUserId = recipientUserId;
        this.content = content;
    }
    
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
}
