-- 创建文章草稿表（审核系统）
-- 版本: V006
-- 日期: 2025-07-31
-- 描述: 为文章审核系统创建草稿表

CREATE TABLE IF NOT EXISTS `article_drafts` (
  `draft_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '草稿ID',
  `article_id` BIGINT COMMENT '关联的文章ID（编辑现有文章时）',
  `editor_user_id` BIGINT NOT NULL COMMENT '编辑者用户ID',
  `title` VARCHAR(255) NOT NULL COMMENT '文章标题',
  `content` LONGTEXT COMMENT '文章内容',
  `category` VARCHAR(50) COMMENT '文章分类',
  `tags` TEXT COMMENT '文章标签',
  `review_status` ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
  `submitted_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `reviewed_at` TIMESTAMP NULL COMMENT '审核时间',
  `reviewer_user_id` BIGINT COMMENT '审核者用户ID',
  `review_notes` TEXT COMMENT '审核备注',
  `edit_notes` TEXT COMMENT '编辑说明',
  INDEX `idx_article_id` (`article_id`),
  INDEX `idx_editor_user_id` (`editor_user_id`),
  INDEX `idx_review_status` (`review_status`),
  INDEX `idx_submitted_at` (`submitted_at`),
  INDEX `idx_reviewer_user_id` (`reviewer_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章草稿表（审核系统）';
