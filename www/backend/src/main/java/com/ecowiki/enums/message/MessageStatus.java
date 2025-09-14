/**
 * 消息状态枚举类
 * 
 * 功能：
 * - 定义消息的各种状态
 * - 提供状态转换验证
 * - 支持状态的国际化显示
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.enums.message;

public enum MessageStatus {
    /**
     * 未读状态
     */
    UNREAD("未读", "Message has not been read"),
    
    /**
     * 已读状态
     */
    VIEWED("已读", "Message has been read"),
    
    /**
     * 已删除状态（软删除）
     */
    DELETED("已删除", "Message has been deleted"),
    
    /**
     * 撤回状态
     */
    RECALLED("已撤回", "Message has been recalled");
    
    private final String chineseDescription;
    private final String englishDescription;
    
    MessageStatus(String chineseDescription, String englishDescription) {
        this.chineseDescription = chineseDescription;
        this.englishDescription = englishDescription;
    }
    
    /**
     * 获取中文描述
     */
    public String getChineseDescription() {
        return chineseDescription;
    }
    
    /**
     * 获取英文描述
     */
    public String getEnglishDescription() {
        return englishDescription;
    }
    
    /**
     * 从字符串转换为枚举
     */
    public static MessageStatus fromString(String status) {
        if (status == null) {
            return UNREAD;
        }
        
        // 兼容旧版本的字符串状态
        switch (status) {
            case "未读":
                return UNREAD;
            case "已读":
                return VIEWED;
            case "已删除":
                return DELETED;
            case "已撤回":
                return RECALLED;
            default:
                try {
                    return MessageStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return UNREAD;
                }
        }
    }
    
    /**
     * 检查状态转换是否有效
     */
    public boolean canTransitionTo(MessageStatus newStatus) {
        switch (this) {
            case UNREAD:
                return newStatus == VIEWED || newStatus == DELETED;
            case VIEWED:
                return newStatus == DELETED;
            case DELETED:
                return false; // 已删除的消息不能转换到其他状态
            case RECALLED:
                return false; // 已撤回的消息不能转换到其他状态
            default:
                return false;
        }
    }
}