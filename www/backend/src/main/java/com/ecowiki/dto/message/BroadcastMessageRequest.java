package com.ecowiki.dto.message;

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

    // Getter方法
    public List<Integer> getRecipientUserIds() {
        return recipientUserIds;
    }

    public String getContent() {
        return content;
    }

    // Setter方法
    public void setRecipientUserIds(List<Integer> recipientUserIds) {
        this.recipientUserIds = recipientUserIds;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
