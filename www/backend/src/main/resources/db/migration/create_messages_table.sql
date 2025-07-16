-- 创建消息通知表
CREATE TABLE IF NOT EXISTS `messages` (
  `message_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
  `recipient_user_id` INT NOT NULL COMMENT '接收用户ID',
  `sender_user_id` INT NOT NULL COMMENT '发送用户ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `send_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `status` VARCHAR(50) DEFAULT '未读' COMMENT '消息状态：未读、已读',
  INDEX `idx_recipient_user_id` (`recipient_user_id`),
  INDEX `idx_sender_user_id` (`sender_user_id`),
  INDEX `idx_send_time` (`send_time`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';
