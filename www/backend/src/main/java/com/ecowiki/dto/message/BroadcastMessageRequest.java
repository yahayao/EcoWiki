package com.ecowiki.dto.message;

import java.util.List;

/**
 * 群发消息请求DTO
 * 
 * 用于管理员或具有权限的用户向多个用户同时发送消息的数据传输对象。
 * 
 * @author EcoWiki
 * @version 1.0
 * @since 2025-07-31
 */
public class BroadcastMessageRequest {
    
    /** 接收用户ID列表 - 指定要接收消息的所有用户ID */
    private List<Integer> recipientUserIds;
    
    /** 消息内容 - 要发送给所有接收者的消息文本 */
    private String content;
    
    /**
     * 默认构造函数
     */
    public BroadcastMessageRequest() {}
    
    /**
     * 全参数构造函数
     * @param recipientUserIds 接收用户ID列表，不能为空
     * @param content 消息内容，不能为空
     */
    public BroadcastMessageRequest(List<Integer> recipientUserIds, String content) {
        this.recipientUserIds = recipientUserIds;
        this.content = content;
    }

    // Getter 和 Setter 方法
    
    /**
     * 获取接收用户ID列表
     * @return 接收用户ID列表
     */
    public List<Integer> getRecipientUserIds() {
        return recipientUserIds;
    }

    /**
     * 获取消息内容
     * @return 消息内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置接收用户ID列表
     * @param recipientUserIds 接收用户ID列表，不能为空
     */
    public void setRecipientUserIds(List<Integer> recipientUserIds) {
        this.recipientUserIds = recipientUserIds;
    }

    /**
     * 设置消息内容
     * @param content 消息内容，不能为空
     */
    public void setContent(String content) {
        this.content = content;
    }
}
