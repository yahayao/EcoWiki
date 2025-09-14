/**
 * 消息类型枚举类
 * 
 * 功能：
 * - 定义不同类型的消息
 * - 支持系统消息和用户消息的区分
 * - 提供消息优先级和处理方式
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.enums.message;

public enum MessageType {
    /**
     * 用户私信
     */
    USER_PRIVATE("用户私信", MessagePriority.NORMAL, false),
    
    /**
     * 系统通知
     */
    SYSTEM_NOTIFICATION("系统通知", MessagePriority.HIGH, true),
    
    /**
     * 文章审核通知
     */
    ARTICLE_REVIEW("文章审核通知", MessagePriority.HIGH, true),
    
    /**
     * 评论回复通知
     */
    COMMENT_REPLY("评论回复通知", MessagePriority.NORMAL, true),
    
    /**
     * 群组消息
     */
    GROUP_MESSAGE("群组消息", MessagePriority.NORMAL, false),
    
    /**
     * 公告消息
     */
    ANNOUNCEMENT("公告消息", MessagePriority.URGENT, true);
    
    private final String description;
    private final MessagePriority priority;
    private final boolean isSystemGenerated;
    
    MessageType(String description, MessagePriority priority, boolean isSystemGenerated) {
        this.description = description;
        this.priority = priority;
        this.isSystemGenerated = isSystemGenerated;
    }
    
    /**
     * 获取消息类型描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 获取消息优先级
     */
    public MessagePriority getPriority() {
        return priority;
    }
    
    /**
     * 是否为系统生成的消息
     */
    public boolean isSystemGenerated() {
        return isSystemGenerated;
    }
    
    /**
     * 消息优先级枚举
     */
    public enum MessagePriority {
        LOW(1, "低优先级"),
        NORMAL(2, "普通优先级"),
        HIGH(3, "高优先级"),
        URGENT(4, "紧急优先级");
        
        private final int level;
        private final String description;
        
        MessagePriority(int level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public int getLevel() {
            return level;
        }
        
        public String getDescription() {
            return description;
        }
    }
}