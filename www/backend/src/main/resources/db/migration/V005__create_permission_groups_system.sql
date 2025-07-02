-- Wiki.js风格的权限分组和细分权限系统
-- 此脚本将重构权限系统，采用分组-权限的二级结构

-- 1. 创建权限分组表
CREATE TABLE permission_group (
    group_id INT PRIMARY KEY AUTO_INCREMENT,
    group_key VARCHAR(50) NOT NULL UNIQUE COMMENT '分组标识符，如content、users、admin',
    group_name VARCHAR(100) NOT NULL COMMENT '分组显示名称',
    group_description TEXT COMMENT '分组描述',
    icon VARCHAR(50) COMMENT '分组图标',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 重构权限表，添加分组关联
ALTER TABLE permission 
ADD COLUMN group_id INT COMMENT '所属权限分组ID',
ADD COLUMN permission_key VARCHAR(100) COMMENT '权限标识符，如read:pages、write:users',
ADD COLUMN is_system BOOLEAN DEFAULT FALSE COMMENT '是否为系统内置权限',
ADD COLUMN sort_order INT DEFAULT 0 COMMENT '在分组内的排序',
ADD FOREIGN KEY (group_id) REFERENCES permission_group(group_id) ON DELETE SET NULL;

-- 3. 插入Wiki.js风格的权限分组
INSERT INTO permission_group (group_key, group_name, group_description, icon, sort_order) VALUES
('content', '内容管理', '页面、文章的创建、编辑、删除等权限', 'content-copy', 1),
('users', '用户管理', '用户账户、角色、权限的管理', 'account-group', 2),
('admin', '系统管理', '系统设置、配置、维护等管理权限', 'cog', 3),
('navigation', '导航管理', '菜单、导航结构的管理', 'menu', 4),
('assets', '资源管理', '文件、图片、媒体资源的管理', 'folder-image', 5);

-- 4. 插入细分权限（基于Wiki.js的权限模式）
-- 内容管理权限
INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '查看页面', '查看所有页面内容', 'read:pages', group_id, 1, TRUE FROM permission_group WHERE group_key = 'content';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '创建页面', '创建新页面', 'write:pages', group_id, 2, TRUE FROM permission_group WHERE group_key = 'content';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '编辑页面', '编辑现有页面', 'manage:pages', group_id, 3, TRUE FROM permission_group WHERE group_key = 'content';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '删除页面', '删除页面', 'delete:pages', group_id, 4, TRUE FROM permission_group WHERE group_key = 'content';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '页面历史', '查看和管理页面历史版本', 'read:history', group_id, 5, TRUE FROM permission_group WHERE group_key = 'content';

-- 用户管理权限
INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '查看用户', '查看用户列表和信息', 'read:users', group_id, 1, TRUE FROM permission_group WHERE group_key = 'users';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '创建用户', '创建新用户账户', 'write:users', group_id, 2, TRUE FROM permission_group WHERE group_key = 'users';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '编辑用户', '编辑用户信息', 'manage:users', group_id, 3, TRUE FROM permission_group WHERE group_key = 'users';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '删除用户', '删除用户账户', 'delete:users', group_id, 4, TRUE FROM permission_group WHERE group_key = 'users';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '管理角色', '创建、编辑、删除角色', 'manage:roles', group_id, 5, TRUE FROM permission_group WHERE group_key = 'users';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '分配权限', '为角色分配权限', 'assign:permissions', group_id, 6, TRUE FROM permission_group WHERE group_key = 'users';

-- 系统管理权限
INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '系统配置', '修改系统设置和配置', 'manage:system', group_id, 1, TRUE FROM permission_group WHERE group_key = 'admin';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '查看日志', '查看系统运行日志', 'read:logs', group_id, 2, TRUE FROM permission_group WHERE group_key = 'admin';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '数据库管理', '数据库备份、恢复等操作', 'manage:database', group_id, 3, TRUE FROM permission_group WHERE group_key = 'admin';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '主题管理', '管理网站主题和样式', 'manage:themes', group_id, 4, TRUE FROM permission_group WHERE group_key = 'admin';

-- 导航管理权限
INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '查看导航', '查看导航结构', 'read:navigation', group_id, 1, TRUE FROM permission_group WHERE group_key = 'navigation';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '编辑导航', '编辑导航菜单和结构', 'manage:navigation', group_id, 2, TRUE FROM permission_group WHERE group_key = 'navigation';

-- 资源管理权限
INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '查看资源', '查看文件和媒体资源', 'read:assets', group_id, 1, TRUE FROM permission_group WHERE group_key = 'assets';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '上传资源', '上传文件和媒体', 'write:assets', group_id, 2, TRUE FROM permission_group WHERE group_key = 'assets';

INSERT INTO permission (permission_name, description, permission_key, group_id, sort_order, is_system) 
SELECT '管理资源', '删除、重命名文件', 'manage:assets', group_id, 3, TRUE FROM permission_group WHERE group_key = 'assets';

-- 5. 创建视图，方便查询权限分组和权限的关联信息
CREATE VIEW v_permission_groups AS
SELECT 
    pg.group_id,
    pg.group_key,
    pg.group_name,
    pg.group_description,
    pg.icon,
    pg.sort_order as group_sort_order,
    pg.is_active as group_active,
    p.permission_id,
    p.permission_name,
    p.description as permission_description,
    p.permission_key,
    p.sort_order as permission_sort_order,
    p.is_system
FROM permission_group pg
LEFT JOIN permission p ON pg.group_id = p.group_id
WHERE pg.is_active = TRUE
ORDER BY pg.sort_order, p.sort_order;

-- 6. 为新字段创建索引
CREATE INDEX idx_permission_group_id ON permission(group_id);
CREATE INDEX idx_permission_key ON permission(permission_key);
CREATE INDEX idx_permission_group_key ON permission_group(group_key);
