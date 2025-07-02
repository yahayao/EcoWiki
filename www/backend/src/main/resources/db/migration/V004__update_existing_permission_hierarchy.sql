-- 为现有的权限表添加父权限字段支持
-- 基于当前权限表的数据结构进行优化

-- 1. 添加父权限字段
ALTER TABLE permission ADD COLUMN parent_permission_id INT NULL;

-- 2. 添加外键约束
ALTER TABLE permission 
ADD CONSTRAINT fk_permission_parent 
FOREIGN KEY (parent_permission_id) REFERENCES permission(permission_id)
ON DELETE SET NULL;

-- 3. 添加索引提高查询性能
CREATE INDEX idx_permission_parent ON permission(parent_permission_id);

-- 4. 基于现有权限创建层次结构
-- 假设当前的6个权限分别是：查看文章、编辑文章、删除文章、发表评论、查看评论、删除评论

-- 先创建顶级权限分类
INSERT INTO permission (permission_name, description, parent_permission_id, created_at, updated_at) VALUES
('content_management', '内容管理', NULL, NOW(), NOW()),
('comment_management', '评论管理', NULL, NOW(), NOW());

-- 更新现有权限，将它们分配到合适的父权限下
-- 注意：需要根据实际的权限名称调整

-- 假设前3个是文章相关权限，将它们归到content_management下
UPDATE permission 
SET parent_permission_id = (SELECT permission_id FROM permission WHERE permission_name = 'content_management' LIMIT 1)
WHERE permission_id IN (1, 2, 3);

-- 假设后3个是评论相关权限，将它们归到comment_management下  
UPDATE permission 
SET parent_permission_id = (SELECT permission_id FROM permission WHERE permission_name = 'comment_management' LIMIT 1)
WHERE permission_id IN (4, 5, 6);
