-- 权限和角色初始化脚本
-- 此脚本用于初始化权限系统的基础数据
-- 基于实际数据库表结构：permission、role、role_permissions

-- 检查当前数据状态
SELECT 'Current permissions:' AS info;
SELECT permission_id, permission_name, description FROM permission ORDER BY permission_id;

SELECT 'Current roles:' AS info;
SELECT role_id, role_name, description FROM role ORDER BY role_id;

SELECT 'Current role permissions:' AS info;
SELECT rp.role, rp.permission_id, p.permission_name 
FROM role_permissions rp 
LEFT JOIN permission p ON rp.permission_id = p.permission_id 
ORDER BY rp.role, rp.permission_id;

-- 插入基础权限（如果不存在）
INSERT IGNORE INTO permission (permission_name, description) VALUES
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
('数据备份', '允许执行数据备份操作');

-- 确保基础角色存在（如果不存在）
INSERT IGNORE INTO role (role_name, description) VALUES
('user', '默认用户，可浏览文章、发表评论'),
('moderator', '版主，可管理文章和评论'),
('admin', '管理员，可管理用户和权限'),
('superadmin', '超级管理员，拥有全部权限');

-- 为角色分配基础权限
-- 普通用户权限
INSERT IGNORE INTO role_permissions (role, permission_id)
SELECT 1, p.permission_id FROM permission p WHERE p.permission_name IN ('查看文章', '发表评论', '查看评论');

-- 版主权限（包含普通用户权限）
INSERT IGNORE INTO role_permissions (role, permission_id)
SELECT 2, p.permission_id FROM permission p WHERE p.permission_name IN ('查看文章', '编辑文章', '发表评论', '查看评论', '删除评论');

-- 管理员权限（包含版主权限 + 用户管理）
INSERT IGNORE INTO role_permissions (role, permission_id)
SELECT 3, p.permission_id FROM permission p WHERE p.permission_name IN (
    '查看文章', '编辑文章', '删除文章', '发表评论', '查看评论', '删除评论',
    '查看用户', '编辑用户', '管理角色', '分配权限', '系统设置'
);

-- 超级管理员权限（全部权限）
INSERT IGNORE INTO role_permissions (role, permission_id)
SELECT 4, p.permission_id FROM permission p;

-- 验证插入结果
SELECT 'Permissions after initialization:' AS info;
SELECT permission_id, permission_name, description FROM permission ORDER BY permission_id;

SELECT 'Roles after initialization:' AS info;
SELECT role_id, role_name, description FROM role ORDER BY role_id;

SELECT 'Role permissions summary:' AS info;
SELECT 
    r.role_name,
    COUNT(rp.permission_id) as permission_count,
    GROUP_CONCAT(p.permission_name ORDER BY p.permission_name SEPARATOR ', ') as permissions
FROM role r 
LEFT JOIN role_permissions rp ON r.role_id = rp.role
LEFT JOIN permission p ON rp.permission_id = p.permission_id
GROUP BY r.role_id, r.role_name
ORDER BY r.role_id;
