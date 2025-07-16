package com.ecowiki.dto;

import java.util.List;

/**
 * 群发消息请求DTO
 */
public class BroadcastMessageRequest {
    
    /** 接收用户ID列表 */
    private List<Integer> recipientUserIds;
    
    /** 消息内容 */
    private String content;
    
    public BroadcastMessageRequest() {}
    
    public BroadcastMessageRequest(List<Integer> recipientUserIds, String content) {
        this.recipientUserIds = recipientUserIds;
        this.content = content;
    }
    
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
}
