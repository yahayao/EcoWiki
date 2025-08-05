package com.ecowiki.dto.message;

/**
 * 发送消息请求DTO
 * 
 * 用于用户向指定用户发送单条消息的数据传输对象。
 * 包含接收者信息和消息内容。
 * 
 * @author EcoWiki
 * @version 1.0
 * @since 2025-07-31
 */
public class SendMessageRequest {
    
    /** 接收者用户ID */
    private Integer recipientUserId;
    
    /** 消息内容 */
    private String content;
    
    /**
     * 默认构造函数
     */
    public SendMessageRequest() {}
    
    /**
     * 全参数构造函数
     * @param recipientUserId 接收者用户ID，不能为空
     * @param content 消息内容，不能为空
     */
    public SendMessageRequest(Integer recipientUserId, String content) {
        this.recipientUserId = recipientUserId;
        this.content = content;
    }

    // Getter 和 Setter 方法
    
    /**
     * 获取接收者用户ID
     * @return 接收者用户ID
     */
    public Integer getRecipientUserId() {
        return recipientUserId;
    }

    /**
     * 获取消息内容
     * @return 消息内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置接收者用户ID
     * @param recipientUserId 接收者用户ID，不能为空
     */
    public void setRecipientUserId(Integer recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    /**
     * 设置消息内容
     * @param content 消息内容，不能为空
     */
    public void setContent(String content) {
        this.content = content;
    }
}
