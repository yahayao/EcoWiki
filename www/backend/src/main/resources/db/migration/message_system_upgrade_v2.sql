-- Active: 1750348433185@@sh-cynosdbmysql-grp-2yvrUPDATE `messages` 
SET `status` = CASE 
    WHEN `status` = '未读' THEN 'UNREAD'
    WHEN `status` = '已读' THEN 'VIEWED'
    WHEN `status` = '已删    IF OLD.status = 'UNREAD' AND NEW.status = 'VIEWED' AND NEW.read_time IS NULL THEN' THEN 'DELETED'
    WHEN `status` = '已撤回' THEN 'RECALLED'
    ELSE 'UNREAD'
END;

ALTER TABLE `messages` 
MODIFY COLUMN `status` ENUM('UNREAD', 'VIEWED', 'DELETED', 'RECALLED') DEFAULT 'UNREAD' COMMENT '消息状态';ncentcdb.com@26809@Ecosql
-- ==========================================
-- EcoWiki 消息系统重构 - 数据库升级脚本
-- 版本: 2.0.0
-- 日期: 2025-09-14
-- 说明: 将现有的消息系统升级为更强大的版本
-- ==========================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 备份现有消息表 （GTID 安全）
-- 使用 CREATE TABLE ... LIKE 创建表结构，然后通过 INSERT INTO ... SELECT 复制数据，避免 GTID 不兼容的 CREATE TABLE ... SELECT
CREATE TABLE IF NOT EXISTS `messages_backup_v1` LIKE `messages`;
INSERT INTO `messages_backup_v1` SELECT * FROM `messages`;

-- 为现有消息表添加新字段
ALTER TABLE `messages` 
ADD COLUMN `message_type` ENUM(
    'USER_PRIVATE', 
    'SYSTEM_NOTIFICATION', 
    'ARTICLE_REVIEW', 
    'COMMENT_REPLY', 
    'GROUP_MESSAGE', 
    'ANNOUNCEMENT'
) DEFAULT 'USER_PRIVATE' COMMENT '消息类型' AFTER `status`;

ALTER TABLE `messages` 
ADD COLUMN `priority` INT DEFAULT 2 COMMENT '消息优先级（1-4）' AFTER `message_type`;

ALTER TABLE `messages` 
ADD COLUMN `expire_time` DATETIME NULL COMMENT '过期时间' AFTER `priority`;

ALTER TABLE `messages` 
ADD COLUMN `read_time` DATETIME NULL COMMENT '已读时间' AFTER `expire_time`;

ALTER TABLE `messages` 
ADD COLUMN `subject` VARCHAR(255) NULL COMMENT '消息主题' AFTER `read_time`;

ALTER TABLE `messages` 
ADD COLUMN `metadata` JSON NULL COMMENT '消息元数据' AFTER `subject`;

ALTER TABLE `messages` 
ADD COLUMN `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `metadata`;

ALTER TABLE `messages` 
ADD COLUMN `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`;

-- 在将字段转换为 ENUM 之前，先将现有的中文/其它字符串值转换为目标枚举值，避免 ALTER 时的数据截断问题
UPDATE `messages` 
SET `status` = CASE 
    WHEN `status` = '未读' THEN 'UNREAD'
    WHEN `status` = '已读' THEN 'READ'
    WHEN `status` = '已删除' THEN 'DELETED'
    WHEN `status` = '已撤回' THEN 'RECALLED'
    ELSE 'UNREAD'
END;

ALTER TABLE `messages` 
MODIFY COLUMN `status` ENUM('UNREAD', 'READ', 'DELETED', 'RECALLED') DEFAULT 'UNREAD' COMMENT '消息状态';

-- 添加新的索引以提高查询性能
CREATE INDEX `idx_message_type` ON `messages` (`message_type`);
CREATE INDEX `idx_priority` ON `messages` (`priority`);
CREATE INDEX `idx_expire_time` ON `messages` (`expire_time`);
CREATE INDEX `idx_read_time` ON `messages` (`read_time`);
CREATE INDEX `idx_created_at` ON `messages` (`created_at`);
CREATE INDEX `idx_updated_at` ON `messages` (`updated_at`);

-- 组合索引优化常见查询
CREATE INDEX `idx_recipient_status_type` ON `messages` (`recipient_user_id`, `status`, `message_type`);
CREATE INDEX `idx_sender_status_type` ON `messages` (`sender_user_id`, `status`, `message_type`);
CREATE INDEX `idx_visible_messages` ON `messages` (`recipient_user_id`, `status`, `expire_time`);
CREATE INDEX `idx_conversation` ON `messages` (`sender_user_id`, `recipient_user_id`, `send_time`);

-- 数据迁移：更新现有数据
-- 1. 将旧的字符串状态转换为枚举状态（已在上方执行以避免 ALTER 时的数据截断）

-- 2. 设置默认消息类型和优先级
UPDATE `messages` 
SET `message_type` = 'USER_PRIVATE', 
    `priority` = 2,
    `created_at` = `send_time`,
    `updated_at` = `send_time`
WHERE `message_type` IS NULL;

-- 3. 为已读消息设置读取时间（估算值，基于发送时间）
UPDATE `messages` 
SET `read_time` = DATE_ADD(`send_time`, INTERVAL FLOOR(RAND() * 1440) MINUTE)
WHERE `status` = 'VIEWED' AND `read_time` IS NULL;

-- 创建消息统计视图（可选，用于快速统计）
CREATE OR REPLACE VIEW `message_statistics` AS
SELECT 
    u.user_id,
    u.username,
    COUNT(CASE WHEN m.status = 'UNREAD' THEN 1 END) as unread_count,
    COUNT(CASE WHEN m.status = 'VIEWED' THEN 1 END) as read_count,
    COUNT(CASE WHEN m.message_type = 'SYSTEM_NOTIFICATION' THEN 1 END) as system_notifications,
    COUNT(CASE WHEN m.message_type = 'USER_PRIVATE' THEN 1 END) as private_messages,
    MAX(m.send_time) as last_message_time
FROM `user` u
LEFT JOIN `messages` m ON u.user_id = m.recipient_user_id 
    AND m.status != 'DELETED' 
    AND (m.expire_time IS NULL OR m.expire_time > NOW())
GROUP BY u.user_id, u.username;

-- 创建存储过程：清理过期消息
DELIMITER $$

CREATE PROCEDURE `CleanupExpiredMessages`()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE msg_id INT;
    DECLARE cur CURSOR FOR 
        SELECT message_id 
        FROM `messages` 
        WHERE expire_time IS NOT NULL 
          AND expire_time <= NOW() 
          AND status != 'DELETED';
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    cleanup_loop: LOOP
        FETCH cur INTO msg_id;
        IF done THEN
            LEAVE cleanup_loop;
        END IF;
        
        UPDATE `messages` 
        SET status = 'DELETED', updated_at = NOW() 
        WHERE message_id = msg_id;
    END LOOP;
    
    CLOSE cur;
    
    -- 返回清理的消息数量
    SELECT ROW_COUNT() as cleaned_messages;
END$$

DELIMITER ;

-- 创建触发器：自动设置优先级
DELIMITER $$

CREATE TRIGGER `set_message_priority` 
BEFORE INSERT ON `messages`
FOR EACH ROW
BEGIN
    IF NEW.priority IS NULL THEN
        SET NEW.priority = CASE NEW.message_type
            WHEN 'ANNOUNCEMENT' THEN 4
            WHEN 'SYSTEM_NOTIFICATION' THEN 3
            WHEN 'ARTICLE_REVIEW' THEN 3
            WHEN 'COMMENT_REPLY' THEN 2
            WHEN 'GROUP_MESSAGE' THEN 2
            WHEN 'USER_PRIVATE' THEN 2
            ELSE 2
        END;
    END IF;
    
    IF NEW.created_at IS NULL THEN
        SET NEW.created_at = NOW();
    END IF;
    
    IF NEW.updated_at IS NULL THEN
        SET NEW.updated_at = NOW();
    END IF;
END$$

DELIMITER ;

-- 创建触发器：自动更新时间戳
DELIMITER $$

CREATE TRIGGER `update_message_timestamp` 
BEFORE UPDATE ON `messages`
FOR EACH ROW
BEGIN
    SET NEW.updated_at = NOW();
    
    -- 如果状态从未读变为已读，设置读取时间
    IF OLD.status = 'UNREAD' AND NEW.status = 'READ' AND NEW.read_time IS NULL THEN
        SET NEW.read_time = NOW();
    END IF;
END$$

DELIMITER ;

-- 插入系统配置数据
INSERT INTO `messages` 
(`recipient_user_id`, `sender_user_id`, `content`, `subject`, `message_type`, `priority`, `status`, `send_time`) 
SELECT 
    u.user_id, 
    0, 
    '欢迎使用EcoWiki全新的消息系统！新系统支持多种消息类型、优先级管理、消息过期等功能。', 
    '消息系统升级通知', 
    'SYSTEM_NOTIFICATION', 
    3, 
    'UNREAD', 
    NOW()
FROM `user` u 
WHERE u.active = 1
  AND NOT EXISTS (
      SELECT 1 FROM `messages` m 
      WHERE m.recipient_user_id = u.user_id 
        AND m.subject = '消息系统升级通知'
  );

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 优化表
OPTIMIZE TABLE `messages`;

-- 显示升级完成信息
SELECT '===========================================' as message;
SELECT 'EcoWiki 消息系统升级完成！' as message;
SELECT '版本: 2.0.0' as message;
SELECT CONCAT('升级时间: ', NOW()) as message;
SELECT '新功能:' as message;
SELECT '- 支持多种消息类型' as feature;
SELECT '- 消息优先级管理' as feature;
SELECT '- 消息过期机制' as feature;
SELECT '- 软删除和撤回' as feature;
SELECT '- 元数据支持' as feature;
SELECT '- 性能优化索引' as feature;
SELECT '===========================================' as message;

-- 显示统计信息
SELECT 'Messages migrated:' as info, COUNT(*) as count FROM `messages`;
SELECT 'Unread messages:' as info, COUNT(*) as count FROM `messages` WHERE status = 'UNREAD';
SELECT 'System notifications sent:' as info, COUNT(*) as count FROM `messages` WHERE message_type = 'SYSTEM_NOTIFICATION';