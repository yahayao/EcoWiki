-- =========================================
-- EcoWiki 文章审核系统数据库初始化脚本
-- 版本: 1.0
-- 日期: 2025-07-25
-- =========================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================
-- 1. 创建审核相关表结构
-- =========================================

-- 文章审核表
CREATE TABLE IF NOT EXISTS `article_review` (
  `review_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `reviewer_id` BIGINT COMMENT '审核员ID',
  `submitter_id` BIGINT NOT NULL COMMENT '提交人ID',
  `review_type` ENUM('CREATE', 'UPDATE', 'DELETE') NOT NULL COMMENT '审核类型',
  `status` ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
  `content_snapshot` LONGTEXT COMMENT '内容快照',
  `review_reason` TEXT COMMENT '审核原因/备注',
  `submit_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `review_time` TIMESTAMP COMMENT '审核时间',
  `auto_publish` BOOLEAN DEFAULT FALSE COMMENT '是否自动发布',
  `priority_level` TINYINT DEFAULT 1 COMMENT '优先级（1-5）',
  `review_deadline` TIMESTAMP COMMENT '审核截止时间',
  INDEX `idx_article_id` (`article_id`),
  INDEX `idx_reviewer_id` (`reviewer_id`),
  INDEX `idx_submitter_id` (`submitter_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_submit_time` (`submit_time`),
  INDEX `idx_review_type` (`review_type`),
  INDEX `idx_deadline` (`review_deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章审核表';

-- 审核权限配置表
CREATE TABLE IF NOT EXISTS `review_permission_config` (
  `config_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `can_review_create` BOOLEAN DEFAULT FALSE COMMENT '可审核创建',
  `can_review_update` BOOLEAN DEFAULT FALSE COMMENT '可审核更新', 
  `can_review_delete` BOOLEAN DEFAULT FALSE COMMENT '可审核删除',
  `can_assign_reviewer` BOOLEAN DEFAULT FALSE COMMENT '可指定审核员',
  `auto_assign_priority` TINYINT DEFAULT 1 COMMENT '自动分配优先级',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核权限配置表';

-- 审核员分配表
CREATE TABLE IF NOT EXISTS `reviewer_assignment` (
  `assignment_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分配ID',
  `review_id` BIGINT NOT NULL COMMENT '审核ID',
  `reviewer_id` BIGINT NOT NULL COMMENT '审核员ID',
  `assigned_by` BIGINT NOT NULL COMMENT '分配人ID',
  `assigned_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `status` ENUM('ASSIGNED', 'ACCEPTED', 'DECLINED') DEFAULT 'ASSIGNED' COMMENT '分配状态',
  INDEX `idx_review_id` (`review_id`),
  INDEX `idx_reviewer_id` (`reviewer_id`),
  INDEX `idx_assigned_by` (`assigned_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核员分配表';

-- 审核历史记录表
CREATE TABLE IF NOT EXISTS `review_history` (
  `history_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史记录ID',
  `review_id` BIGINT NOT NULL COMMENT '审核ID',
  `action` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `old_status` VARCHAR(20) COMMENT '原状态',
  `new_status` VARCHAR(20) COMMENT '新状态',
  `comment` TEXT COMMENT '操作备注',
  `operation_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  INDEX `idx_review_id` (`review_id`),
  INDEX `idx_operator_id` (`operator_id`),
  INDEX `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核历史记录表';

-- =========================================
-- 2. 添加审核相关权限
-- =========================================

-- 添加审核相关权限
INSERT IGNORE INTO `permission` (`permission_name`, `description`) VALUES
('审核文章创建', '审核新文章的创建请求'),
('审核文章更新', '审核文章的修改请求'),
('审核文章删除', '审核文章的删除请求'),
('指定审核员', '为审核任务指定特定审核员'),
('查看审核列表', '查看待审核和已审核的任务列表'),
('管理审核配置', '管理审核流程的配置设置'),
('审核统计查看', '查看审核相关统计信息');

-- =========================================
-- 3. 为角色分配审核权限
-- =========================================

-- 为版主分配基础审核权限 (role_id = 2)
INSERT IGNORE INTO `role_permissions` (`role`, `permission_id`)
SELECT 2, p.permission_id FROM `permission` p 
WHERE p.permission_name IN ('审核文章创建', '审核文章更新', '查看审核列表');

-- 为管理员分配完整审核权限 (role_id = 3)
INSERT IGNORE INTO `role_permissions` (`role`, `permission_id`)
SELECT 3, p.permission_id FROM `permission` p 
WHERE p.permission_name IN (
    '审核文章创建', '审核文章更新', '审核文章删除', 
    '指定审核员', '查看审核列表', '管理审核配置', '审核统计查看'
);

-- 为超级管理员分配所有审核权限 (role_id = 4)
INSERT IGNORE INTO `role_permissions` (`role`, `permission_id`)
SELECT 4, p.permission_id FROM `permission` p 
WHERE p.permission_name LIKE '审核%' OR p.permission_name LIKE '%审核%';

-- =========================================
-- 4. 初始化审核权限配置
-- =========================================

-- 为各角色设置审核权限配置
INSERT IGNORE INTO `review_permission_config` 
(`role_name`, `can_review_create`, `can_review_update`, `can_review_delete`, `can_assign_reviewer`, `auto_assign_priority`) 
VALUES
('moderator', TRUE, TRUE, FALSE, FALSE, 2),
('admin', TRUE, TRUE, TRUE, TRUE, 3),
('superadmin', TRUE, TRUE, TRUE, TRUE, 5);

-- =========================================
-- 5. 创建审核相关视图
-- =========================================

-- 审核统计视图
CREATE OR REPLACE VIEW `v_review_stats` AS
SELECT 
    r.reviewer_id,
    u.username as reviewer_name,
    COUNT(*) as total_reviews,
    SUM(CASE WHEN r.status = 'APPROVED' THEN 1 ELSE 0 END) as approved_count,
    SUM(CASE WHEN r.status = 'REJECTED' THEN 1 ELSE 0 END) as rejected_count,
    SUM(CASE WHEN r.status = 'PENDING' THEN 1 ELSE 0 END) as pending_count,
    AVG(TIMESTAMPDIFF(HOUR, r.submit_time, r.review_time)) as avg_review_hours
FROM `article_review` r
LEFT JOIN `user` u ON r.reviewer_id = u.user_id
WHERE r.reviewer_id IS NOT NULL
GROUP BY r.reviewer_id, u.username;

-- 待审核文章详情视图
CREATE OR REPLACE VIEW `v_pending_reviews_detail` AS
SELECT 
    r.review_id,
    r.article_id,
    a.title as article_title,
    r.review_type,
    r.priority_level,
    r.submit_time,
    r.review_deadline,
    submitter.username as submitter_name,
    reviewer.username as reviewer_name,
    TIMESTAMPDIFF(HOUR, r.submit_time, NOW()) as hours_pending,
    CASE 
        WHEN r.review_deadline < NOW() THEN 'OVERDUE'
        WHEN TIMESTAMPDIFF(HOUR, NOW(), r.review_deadline) <= 2 THEN 'URGENT'
        ELSE 'NORMAL'
    END as urgency_status
FROM `article_review` r
LEFT JOIN `articles` a ON r.article_id = a.article_id
LEFT JOIN `user` submitter ON r.submitter_id = submitter.user_id
LEFT JOIN `user` reviewer ON r.reviewer_id = reviewer.user_id
WHERE r.status = 'PENDING'
ORDER BY r.priority_level DESC, r.submit_time ASC;

-- =========================================
-- 6. 创建存储过程
-- =========================================

DELIMITER $$

-- 自动分配审核员存储过程
CREATE PROCEDURE `AssignReviewer`(
    IN review_id_param BIGINT,
    IN review_type_param VARCHAR(20)
)
BEGIN
    DECLARE reviewer_id_var BIGINT DEFAULT NULL;
    DECLARE assignment_count INT DEFAULT 0;
    
    -- 获取有权限且当前分配任务最少的审核员
    SELECT u.user_id INTO reviewer_id_var
    FROM user u
    JOIN user_roles ur ON u.user_id = ur.user_id
    JOIN role r ON ur.role_id = r.role_id
    JOIN review_permission_config rpc ON r.role_name = rpc.role_name
    LEFT JOIN (
        SELECT reviewer_id, COUNT(*) as pending_count
        FROM article_review 
        WHERE status = 'PENDING' AND reviewer_id IS NOT NULL
        GROUP BY reviewer_id
    ) pending ON u.user_id = pending.reviewer_id
    WHERE u.active = TRUE
    AND (
        (review_type_param = 'CREATE' AND rpc.can_review_create = TRUE) OR
        (review_type_param = 'UPDATE' AND rpc.can_review_update = TRUE) OR
        (review_type_param = 'DELETE' AND rpc.can_review_delete = TRUE)
    )
    ORDER BY COALESCE(pending.pending_count, 0) ASC, RAND()
    LIMIT 1;
    
    -- 更新审核记录
    IF reviewer_id_var IS NOT NULL THEN
        UPDATE article_review 
        SET reviewer_id = reviewer_id_var 
        WHERE review_id = review_id_param;
        
        -- 记录分配历史
        INSERT INTO reviewer_assignment (review_id, reviewer_id, assigned_by, assigned_time)
        VALUES (review_id_param, reviewer_id_var, 1, NOW());
    END IF;
END$$

DELIMITER ;

-- =========================================
-- 7. 重新启用外键检查
-- =========================================

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================
-- 8. 输出初始化完成信息
-- =========================================

SELECT '=====================================' as message;
SELECT 'EcoWiki 文章审核系统初始化完成！' as message;
SELECT '=====================================' as message;

-- 显示权限统计
SELECT 'Review permissions created:' as info, COUNT(*) as count 
FROM `permission` WHERE permission_name LIKE '%审核%';

SELECT 'Review role-permission mappings:' as info, COUNT(*) as count 
FROM `role_permissions` rp 
JOIN `permission` p ON rp.permission_id = p.permission_id 
WHERE p.permission_name LIKE '%审核%';

SELECT 'Review permission configs:' as info, COUNT(*) as count 
FROM `review_permission_config`;

SELECT '=====================================' as message;
SELECT '文章审核系统已就绪，可以开始使用！' as message;
SELECT '=====================================' as message;
