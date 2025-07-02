-- 权限层次结构升级脚本
-- 基于当前权限表数据：查看文章、编辑文章、删除文章、发表评论、查看评论、删除评论
-- 安全升级，保留现有数据

-- ========================================
-- 第一步：备份当前权限表（可选，但强烈推荐）
-- ========================================
-- CREATE TABLE permission_backup AS SELECT * FROM permission;

-- ========================================
-- 第二步：添加父权限字段
-- ========================================
ALTER TABLE permission ADD COLUMN parent_permission_id INT NULL;

-- ========================================
-- 第三步：添加外键约束和索引
-- ========================================
-- 添加外键约束（支持级联设置为NULL）
ALTER TABLE permission 
ADD CONSTRAINT fk_permission_parent 
FOREIGN KEY (parent_permission_id) REFERENCES permission(permission_id)
ON DELETE SET NULL;

-- 添加索引提高查询性能
CREATE INDEX idx_permission_parent ON permission(parent_permission_id);

-- ========================================
-- 第四步：创建顶级权限分类
-- ========================================
-- 插入内容管理顶级权限
INSERT INTO permission (permission_name, description, parent_permission_id, created_at, updated_at) VALUES
('content_management', '内容管理', NULL, NOW(), NOW());

-- 插入评论管理顶级权限
INSERT INTO permission (permission_name, description, parent_permission_id, created_at, updated_at) VALUES
('comment_management', '评论管理', NULL, NOW(), NOW());

-- 插入用户管理顶级权限（为未来扩展）
INSERT INTO permission (permission_name, description, parent_permission_id, created_at, updated_at) VALUES
('user_management', '用户管理', NULL, NOW(), NOW());

-- 插入系统管理顶级权限（为未来扩展）
INSERT INTO permission (permission_name, description, parent_permission_id, created_at, updated_at) VALUES
('system_management', '系统管理', NULL, NOW(), NOW());

-- ========================================
-- 第五步：为现有权限设置父权限
-- ========================================
-- 获取刚创建的父权限ID
SET @content_mgmt_id = (SELECT permission_id FROM permission WHERE permission_name = 'content_management');
SET @comment_mgmt_id = (SELECT permission_id FROM permission WHERE permission_name = 'comment_management');

-- 根据权限名称模式更新父权限关系
-- 文章相关权限归入内容管理
UPDATE permission 
SET parent_permission_id = @content_mgmt_id 
WHERE permission_name LIKE '%文章%' OR permission_name LIKE '%article%';

-- 评论相关权限归入评论管理
UPDATE permission 
SET parent_permission_id = @comment_mgmt_id 
WHERE permission_name LIKE '%评论%' OR permission_name LIKE '%comment%';

-- ========================================
-- 第六步：添加更多细分权限（可选）
-- ========================================
-- 为内容管理添加更多子权限
INSERT INTO permission (permission_name, description, parent_permission_id, created_at, updated_at) VALUES
('article_create', '创建文章', @content_mgmt_id, NOW(), NOW()),
('article_publish', '发布文章', @content_mgmt_id, NOW(), NOW()),
('article_review', '审核文章', @content_mgmt_id, NOW(), NOW());

-- 为评论管理添加更多子权限
INSERT INTO permission (permission_name, description, parent_permission_id, created_at, updated_at) VALUES
('comment_moderate', '审核评论', @comment_mgmt_id, NOW(), NOW()),
('comment_reply', '回复评论', @comment_mgmt_id, NOW(), NOW());

-- 为用户管理添加子权限
INSERT INTO permission (permission_name, description, parent_permission_id, created_at, updated_at) VALUES
('user_create', '创建用户', (SELECT permission_id FROM permission WHERE permission_name = 'user_management'), NOW(), NOW()),
('user_edit', '编辑用户', (SELECT permission_id FROM permission WHERE permission_name = 'user_management'), NOW(), NOW()),
('user_delete', '删除用户', (SELECT permission_id FROM permission WHERE permission_name = 'user_management'), NOW(), NOW()),
('user_view', '查看用户列表', (SELECT permission_id FROM permission WHERE permission_name = 'user_management'), NOW(), NOW()),
('user_role_assign', '分配用户角色', (SELECT permission_id FROM permission WHERE permission_name = 'user_management'), NOW(), NOW());

-- 为系统管理添加子权限
INSERT INTO permission (permission_name, description, parent_permission_id, created_at, updated_at) VALUES
('system_config', '系统配置', (SELECT permission_id FROM permission WHERE permission_name = 'system_management'), NOW(), NOW()),
('system_backup', '系统备份', (SELECT permission_id FROM permission WHERE permission_name = 'system_management'), NOW(), NOW()),
('system_logs', '查看系统日志', (SELECT permission_id FROM permission WHERE permission_name = 'system_management'), NOW(), NOW()),
('permission_manage', '权限管理', (SELECT permission_id FROM permission WHERE permission_name = 'system_management'), NOW(), NOW()),
('role_manage', '角色管理', (SELECT permission_id FROM permission WHERE permission_name = 'system_management'), NOW(), NOW());

-- ========================================
-- 第七步：验证数据完整性
-- ========================================
-- 查看权限层次结构
SELECT 
    p1.permission_id,
    p1.permission_name,
    p1.description,
    p1.parent_permission_id,
    p2.permission_name AS parent_name
FROM permission p1
LEFT JOIN permission p2 ON p1.parent_permission_id = p2.permission_id
ORDER BY p1.parent_permission_id ASC, p1.permission_id ASC;

-- 统计权限数量
SELECT 
    '顶级权限' AS type, 
    COUNT(*) AS count 
FROM permission 
WHERE parent_permission_id IS NULL
UNION ALL
SELECT 
    '子权限' AS type, 
    COUNT(*) AS count 
FROM permission 
WHERE parent_permission_id IS NOT NULL;
