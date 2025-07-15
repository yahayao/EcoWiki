-- =========================================
-- EcoWiki 完整数据库初始化脚本
-- 一键部署文件 - 包含所有必要的表结构和初始数据
-- 版本: 3.0
-- 日期: 2025-01-15
-- =========================================

-- 设置字符集和存储引擎
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================
-- 1. 创建基础表结构
-- =========================================

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `user_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `email` VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
  `full_name` VARCHAR(100) COMMENT '用户全名',
  `active` BOOLEAN DEFAULT TRUE COMMENT '是否激活',
  `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未设置，1-男，2-女',
  `email_verified` BOOLEAN DEFAULT FALSE COMMENT '邮箱验证状态',
  `login_token` VARCHAR(255) COMMENT '登录令牌',
  `role_id` INT COMMENT '临时角色ID字段',
  `permissions` VARCHAR(500) COMMENT '权限描述',
  `last_login` TIMESTAMP COMMENT '最后登录时间',
  `avatar_url` VARCHAR(255) COMMENT '头像URL',
  `bio` TEXT COMMENT '个人简介',
  `security_question` VARCHAR(255) COMMENT '安全问题',
  `security_answer` VARCHAR(255) COMMENT '安全答案',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_username` (`username`),
  INDEX `idx_email` (`email`),
  INDEX `idx_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
  `role_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
  `role_name` VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名称',
  `description` TEXT COMMENT '角色描述',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `permission` (
  `permission_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `description` TEXT COMMENT '权限描述',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_permission_name` (`permission_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id` INT NOT NULL COMMENT '用户ID',
  `role_id` INT NOT NULL COMMENT '角色ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  PRIMARY KEY (`user_id`, `role_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permissions` (
  `role` INT NOT NULL COMMENT '角色ID（兼容旧字段名）',
  `permission_id` INT NOT NULL COMMENT '权限ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  PRIMARY KEY (`role`, `permission_id`),
  INDEX `idx_role` (`role`),
  INDEX `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 文章表
CREATE TABLE IF NOT EXISTS `articles` (
  `article_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
  `title` VARCHAR(255) UNIQUE NOT NULL COMMENT '文章标题',
  `content` LONGTEXT NOT NULL COMMENT '文章内容',
  `author` VARCHAR(100) NOT NULL COMMENT '文章作者',
  `publish_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `category` VARCHAR(100) COMMENT '文章分类',
  `tags` TEXT COMMENT '文章标签（逗号分隔）',
  `views` INT DEFAULT 0 COMMENT '浏览次数',
  `likes` INT DEFAULT 0 COMMENT '点赞数',
  `comments` INT DEFAULT 0 COMMENT '评论数',
  INDEX `idx_title` (`title`),
  INDEX `idx_author` (`author`),
  INDEX `idx_category` (`category`),
  INDEX `idx_publish_date` (`publish_date`),
  INDEX `idx_views` (`views`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 标签表
CREATE TABLE IF NOT EXISTS `tags` (
  `tag_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` VARCHAR(50) UNIQUE NOT NULL COMMENT '标签名称',
  `description` VARCHAR(255) COMMENT '标签描述',
  `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 文章标签关联表
CREATE TABLE IF NOT EXISTS `article_tags` (
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`article_id`, `tag_id`),
  INDEX `idx_article_id` (`article_id`),
  INDEX `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- =========================================
-- 2. 文章版本管理系统（可选）
-- =========================================

-- 文章版本表
CREATE TABLE IF NOT EXISTS `article_versions` (
  `version_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `version_number` INT NOT NULL COMMENT '版本号',
  `author` VARCHAR(100) COMMENT '版本作者',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `storage_type` ENUM('FULL_CONTENT', 'DIFF_FROM_BASE', 'DIFF_FROM_PREV') NOT NULL DEFAULT 'FULL_CONTENT' COMMENT '存储类型',
  `content` LONGBLOB COMMENT '版本内容',
  `content_hash` VARCHAR(64) COMMENT '内容哈希',
  `original_size` BIGINT COMMENT '原始大小',
  `compressed_size` BIGINT COMMENT '压缩后大小',
  `base_version_id` BIGINT COMMENT '基础版本ID',
  `is_compressed` BOOLEAN DEFAULT FALSE COMMENT '是否压缩',
  `is_archived` BOOLEAN DEFAULT FALSE COMMENT '是否归档',
  `change_summary` VARCHAR(500) COMMENT '变更摘要',
  `compression_algorithm` VARCHAR(20) COMMENT '压缩算法',
  INDEX `idx_article_id` (`article_id`),
  INDEX `idx_version_number` (`version_number`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_storage_type` (`storage_type`),
  INDEX `idx_is_archived` (`is_archived`),
  INDEX `idx_content_hash` (`content_hash`),
  INDEX `idx_base_version_id` (`base_version_id`),
  UNIQUE KEY `uk_article_version` (`article_id`, `version_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章版本表';

-- 文章版本统计表
CREATE TABLE IF NOT EXISTS `article_version_stats` (
  `stats_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
  `article_id` BIGINT UNIQUE NOT NULL COMMENT '文章ID',
  `total_versions` INT DEFAULT 0 COMMENT '总版本数',
  `base_versions_count` INT DEFAULT 0 COMMENT '基础版本数',
  `diff_versions_count` INT DEFAULT 0 COMMENT '差异版本数',
  `archived_versions_count` INT DEFAULT 0 COMMENT '归档版本数',
  `total_storage_size` BIGINT DEFAULT 0 COMMENT '总存储大小',
  `compressed_storage_size` BIGINT DEFAULT 0 COMMENT '压缩存储大小',
  `last_optimized_at` TIMESTAMP COMMENT '最后优化时间',
  `last_accessed_at` TIMESTAMP COMMENT '最后访问时间',
  `access_frequency` INT DEFAULT 0 COMMENT '访问频率',
  `optimization_needed` BOOLEAN DEFAULT FALSE COMMENT '是否需要优化',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_article_id` (`article_id`),
  INDEX `idx_last_optimized` (`last_optimized_at`),
  INDEX `idx_last_accessed` (`last_accessed_at`),
  INDEX `idx_optimization_needed` (`optimization_needed`),
  INDEX `idx_total_storage_size` (`total_storage_size`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章版本统计表';

-- 文章版本配置表
CREATE TABLE IF NOT EXISTS `article_version_config` (
  `config_key` VARCHAR(50) PRIMARY KEY COMMENT '配置键',
  `config_value` VARCHAR(255) NOT NULL COMMENT '配置值',
  `description` TEXT COMMENT '配置描述',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章版本配置表';

-- =========================================
-- 3. 插入基础数据
-- =========================================

-- 插入基础角色
INSERT IGNORE INTO `role` (`role_name`, `description`) VALUES
('user', '普通用户，可浏览文章、发表评论'),
('moderator', '版主，可管理文章和评论'),
('admin', '管理员，可管理用户和权限'),
('superadmin', '超级管理员，拥有全部权限');

-- 插入基础权限
INSERT IGNORE INTO `permission` (`permission_name`, `description`) VALUES
('查看文章', '允许用户查看文章内容'),
('编辑文章', '允许用户编辑文章内容'),
('删除文章', '允许用户删除文章'),
('发表评论', '允许用户发表评论'),
('查看评论', '允许用户查看评论'),
('删除评论', '允许用户删除评论'),
('查看用户', '允许查看用户列表和信息'),
('编辑用户', '允许编辑用户信息'),
('删除用户', '允许删除用户账户'),
('管理角色', '允许创建、编辑、删除角色'),
('分配权限', '允许为角色分配权限'),
('系统设置', '允许修改系统设置'),
('查看日志', '允许查看系统日志'),
('数据备份', '允许执行数据备份操作'),
('管理标签', '允许管理文章标签'),
('管理分类', '允许管理文章分类');

-- 为角色分配权限
-- 普通用户权限 (role_id = 1)
INSERT IGNORE INTO `role_permissions` (`role`, `permission_id`)
SELECT 1, p.permission_id FROM `permission` p 
WHERE p.permission_name IN ('查看文章', '查看评论', '发表评论');

-- 版主权限 (role_id = 2)
INSERT IGNORE INTO `role_permissions` (`role`, `permission_id`)
SELECT 2, p.permission_id FROM `permission` p 
WHERE p.permission_name IN ('查看文章', '编辑文章', '查看评论', '发表评论', '删除评论', '管理标签');

-- 管理员权限 (role_id = 3)
INSERT IGNORE INTO `role_permissions` (`role`, `permission_id`)
SELECT 3, p.permission_id FROM `permission` p 
WHERE p.permission_name IN (
    '查看文章', '编辑文章', '删除文章', '查看评论', '发表评论', '删除评论',
    '查看用户', '编辑用户', '管理角色', '分配权限', '管理标签', '管理分类'
);

-- 超级管理员权限 (role_id = 4) - 所有权限
INSERT IGNORE INTO `role_permissions` (`role`, `permission_id`)
SELECT 4, p.permission_id FROM `permission` p;

-- 插入版本配置默认值
INSERT IGNORE INTO `article_version_config` (`config_key`, `config_value`, `description`) VALUES
('diff_threshold_percent', '0.7', '差异超过此比例时存储完整版本'),
('max_versions_per_article', '50', '每篇文章最大版本数'),
('auto_archive_days', '365', '自动归档天数'),
('compression_enabled', 'true', '是否启用压缩'),
('compression_algorithm', 'gzip', '压缩算法'),
('cleanup_archived_days', '1095', '清理归档版本天数（3年）');

-- 创建超级管理员账户
INSERT IGNORE INTO `user` (
    `username`, `password`, `email`, `full_name`, `active`, `email_verified`
) VALUES (
    'superadmin', 'EcoWiki@2025', 'admin@ecowiki.com', '超级管理员', TRUE, TRUE
);

-- 为超级管理员分配角色
INSERT IGNORE INTO `user_roles` (`user_id`, `role_id`)
SELECT u.user_id, 4 FROM `user` u WHERE u.username = 'superadmin';

-- =========================================
-- 4. 创建有用的视图
-- =========================================

-- 权限汇总视图
CREATE OR REPLACE VIEW `v_user_permissions` AS
SELECT 
    u.user_id,
    u.username,
    u.email,
    r.role_name,
    GROUP_CONCAT(p.permission_name ORDER BY p.permission_name SEPARATOR ', ') as permissions
FROM `user` u
LEFT JOIN `user_roles` ur ON u.user_id = ur.user_id
LEFT JOIN `role` r ON ur.role_id = r.role_id
LEFT JOIN `role_permissions` rp ON r.role_id = rp.role
LEFT JOIN `permission` p ON rp.permission_id = p.permission_id
WHERE u.active = TRUE
GROUP BY u.user_id, u.username, u.email, r.role_name;

-- 文章统计视图
CREATE OR REPLACE VIEW `v_article_stats` AS
SELECT 
    a.article_id,
    a.title,
    a.author,
    a.category,
    a.views,
    a.likes,
    a.comments,
    a.publish_date,
    a.update_time,
    COUNT(DISTINCT at.tag_id) as tag_count,
    GROUP_CONCAT(DISTINCT t.tag_name ORDER BY t.tag_name SEPARATOR ', ') as tag_names
FROM `articles` a
LEFT JOIN `article_tags` at ON a.article_id = at.article_id
LEFT JOIN `tags` t ON at.tag_id = t.tag_id
GROUP BY a.article_id;

-- 文章版本统计视图（如果启用版本管理）
CREATE OR REPLACE VIEW `v_article_version_summary` AS
SELECT 
    av.article_id,
    COUNT(*) as total_versions,
    SUM(CASE WHEN av.storage_type = 'FULL_CONTENT' THEN 1 ELSE 0 END) as base_versions,
    SUM(CASE WHEN av.storage_type != 'FULL_CONTENT' THEN 1 ELSE 0 END) as diff_versions,
    SUM(CASE WHEN av.is_archived = TRUE THEN 1 ELSE 0 END) as archived_versions,
    MAX(av.version_number) as latest_version,
    SUM(CASE WHEN av.compressed_size > 0 THEN av.compressed_size ELSE av.original_size END) as total_size,
    MAX(av.created_at) as last_version_date
FROM `article_versions` av
GROUP BY av.article_id;

-- =========================================
-- 5. 创建索引优化
-- =========================================

-- 用户表索引
CREATE INDEX IF NOT EXISTS `idx_user_active_email` ON `user` (`active`, `email`);
CREATE INDEX IF NOT EXISTS `idx_user_last_login` ON `user` (`last_login`);

-- 文章表索引
CREATE INDEX IF NOT EXISTS `idx_articles_category_views` ON `articles` (`category`, `views`);
CREATE INDEX IF NOT EXISTS `idx_articles_author_date` ON `articles` (`author`, `publish_date`);

-- 版本表索引（提升查询性能）
CREATE INDEX IF NOT EXISTS `idx_article_version_chain` ON `article_versions` (`article_id`, `base_version_id`, `version_number`);
CREATE INDEX IF NOT EXISTS `idx_archive_candidates` ON `article_versions` (`is_archived`, `created_at`, `article_id`);

-- =========================================
-- 6. 数据完整性检查
-- =========================================

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 输出初始化完成信息
SELECT '=====================================' as message;
SELECT 'EcoWiki 数据库初始化完成！' as message;
SELECT '=====================================' as message;

-- 显示基础统计信息
SELECT 'Roles created:' as info, COUNT(*) as count FROM `role`;
SELECT 'Permissions created:' as info, COUNT(*) as count FROM `permission`;
SELECT 'Role-Permission mappings:' as info, COUNT(*) as count FROM `role_permissions`;
SELECT 'Default admin user:' as info, username FROM `user` WHERE username = 'superadmin';

SELECT '=====================================' as message;
SELECT '默认超级管理员账户:' as message;
SELECT 'Username: superadmin' as message;
SELECT 'Password: EcoWiki@2025' as message;
SELECT 'Email: admin@ecowiki.com' as message;
SELECT '请登录后立即修改密码！' as message;
SELECT '=====================================' as message;
