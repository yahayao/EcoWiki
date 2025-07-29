package com.ecowiki.dto.message;

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

    // Getter方法
    public Integer getRecipientUserId() {
        return recipientUserId;
    }

    public String getContent() {
        return content;
    }

    // Setter方法
    public void setRecipientUserId(Integer recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
