-- 简化版权限层次结构升级脚本
-- 仅添加必要的字段和基本的父子关系，保持最小变更

-- ========================================
-- 第一步：添加父权限字段
-- ========================================
ALTER TABLE permission ADD COLUMN parent_permission_id INT NULL;

-- ========================================
-- 第二步：添加外键约束和索引
-- ========================================
ALTER TABLE permission 
ADD CONSTRAINT fk_permission_parent 
FOREIGN KEY (parent_permission_id) REFERENCES permission(permission_id)
ON DELETE SET NULL;

CREATE INDEX idx_permission_parent ON permission(parent_permission_id);

-- ========================================
-- 第三步：创建基本的顶级权限分类
-- ========================================
INSERT INTO permission (permission_name, description, parent_permission_id, created_at, updated_at) VALUES
('content_management', '内容管理', NULL, NOW(), NOW()),
('comment_management', '评论管理', NULL, NOW(), NOW());

-- ========================================
-- 第四步：为现有权限设置父权限（基于权限ID）
-- ========================================
-- 假设权限ID 1,2,3 是文章相关权限
UPDATE permission 
SET parent_permission_id = (SELECT permission_id FROM permission WHERE permission_name = 'content_management')
WHERE permission_id IN (1, 2, 3);

-- 假设权限ID 4,5,6 是评论相关权限
UPDATE permission 
SET parent_permission_id = (SELECT permission_id FROM permission WHERE permission_name = 'comment_management')
WHERE permission_id IN (4, 5, 6);

-- ========================================
-- 验证结果
-- ========================================
SELECT 
    p1.permission_id,
    p1.permission_name,
    p1.description,
    p1.parent_permission_id,
    p2.permission_name AS parent_name
FROM permission p1
LEFT JOIN permission p2 ON p1.parent_permission_id = p2.permission_id
ORDER BY p1.parent_permission_id ASC, p1.permission_id ASC;
